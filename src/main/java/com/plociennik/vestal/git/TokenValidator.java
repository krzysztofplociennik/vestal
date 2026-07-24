package com.plociennik.vestal.git;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class TokenValidator {

    private static final String API_BASE = "https://api.github.com";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean existsValidToken() {
        TokenStorage tokenStorage = new TokenStorage();
        Optional<String> load = tokenStorage.load();
        if (load.isEmpty()) {
            return false;
        }
        String token = load.get();
        TokenValidator.AuthResult authResult = this.validate(token);
        return authResult.success();
    }

    private AuthResult validate(String candidateToken) {
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
                    String username = extractLogin(response.body());
                    yield new AuthResult(true, username, null);
                }
                case 401 -> new AuthResult(false, null, "Invalid or expired token.");
                case 403 -> new AuthResult(false, null, "Rate limited or insufficient scope.");
                default -> new AuthResult(false, null, "Unexpected error: HTTP " + response.statusCode());
            };
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
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
