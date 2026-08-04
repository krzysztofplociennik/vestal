package com.plociennik.vestal.login;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
public class TokenValidator {

    private static final String API_BASE = "https://api.github.com";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();

    public AuthResult validate() {
        Optional<String> optionalToken = credentialsStorage.load(CredentialType.GITHUB_TOKEN);
        if (optionalToken.isEmpty()) {
            log.info("[{}] Github token is empty.", "1247_040826");
            return new AuthResult(false, "", "Github token is empty.");
        }
        String token = optionalToken.get();
        return this.authorizeToken(token);
    }

    private AuthResult authorizeToken(String candidateToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/user"))
                    .header("Authorization", "Bearer " + candidateToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> {
                    String login = extractLogin(response.body());
                    yield new AuthResult(true, login, null);
                }
                case 401 -> new AuthResult(false, null, "[401] Invalid or expired token.");
                case 403 -> new AuthResult(false, null, "[403] Rate limited or insufficient scope.");
                default -> new AuthResult(false, null, "Unexpected error: HTTP " + response.statusCode());
            };
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("[{}] Something happened while trying to authorize the GitHub token: {}", "1549_270726", e.getMessage());
            return new AuthResult(false, null, "Network error: " + e.getMessage());
        }
    }

    private String extractLogin(String jsonBody) {
        int idx = jsonBody.indexOf("\"login\"");
        int start = jsonBody.indexOf('"', idx + 8) + 1;
        int end = jsonBody.indexOf('"', start);
        return jsonBody.substring(start, end);
    }

    public record AuthResult(boolean success, String login, String errorMessage) {}

}
