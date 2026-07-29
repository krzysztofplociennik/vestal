package com.plociennik.vestal.git;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitHubDeviceFlow {

    private static final String SCOPE = "repo";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private CredentialStorage credentialStorage = new CredentialStorage();

    public record DeviceCodeResponse(
            String deviceCode, String userCode, String verificationUri,
            int expiresIn, int interval) {}

    public DeviceCodeResponse requestDeviceCode() throws IOException, InterruptedException {
        String body = "client_id=" + credentialStorage.get(CredentialType.GITHUB_CLIENT_ID).get() + "&scope=" + SCOPE;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://github.com/login/device/code"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseDeviceCodeResponse(response.body());
    }

    private DeviceCodeResponse parseDeviceCodeResponse(String json) {
        return new DeviceCodeResponse(
                extract(json, "device_code"),
                extract(json, "user_code"),
                extract(json, "verification_uri"),
                Integer.parseInt(extract(json, "expires_in")),
                Integer.parseInt(extract(json, "interval"))
        );
    }

    private String extract(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        int colon = json.indexOf(':', idx);
        int firstQuoteOrDigit = colon + 1;
        while (json.charAt(firstQuoteOrDigit) == ' ') firstQuoteOrDigit++;
        if (json.charAt(firstQuoteOrDigit) == '"') {
            int start = firstQuoteOrDigit + 1;
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } else {
            int end = firstQuoteOrDigit;
            while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
            return json.substring(firstQuoteOrDigit, end);
        }
    }

    public PollResult pollForToken(DeviceCodeResponse deviceCode) throws IOException, InterruptedException {
        long deadline = System.currentTimeMillis() + deviceCode.expiresIn() * 1000L;
        int intervalSeconds = deviceCode.interval();

        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(intervalSeconds * 1000L);

            String body = "client_id=" + credentialStorage.get(CredentialType.GITHUB_CLIENT_ID).get()
                    + "&device_code=" + deviceCode.deviceCode()
                    + "&grant_type=urn:ietf:params:oauth:grant-type:device_code";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://github.com/login/oauth/access_token"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (responseBody.contains("\"access_token\"")) {
                return new PollResult(true, extract(responseBody, "access_token"), null);
            }

            String error = extract(responseBody, "error");
            switch (error) {
                case "authorization_pending" -> { }
                case "slow_down" -> intervalSeconds += 5;
                case "expired_token" -> {
                    return new PollResult(false, null, "Code expired — please try again.");
                }
                case "access_denied" -> {
                    return new PollResult(false, null, "Login was denied.");
                }
                default -> {
                    return new PollResult(false, null, "Unexpected error: " + error);
                }
            }
        }
        return new PollResult(false, null, "Timed out waiting for authorization.");
    }

    public record PollResult(boolean success, String accessToken, String error) {}
}
