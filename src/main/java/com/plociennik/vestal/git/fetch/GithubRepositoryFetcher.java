package com.plociennik.vestal.git.fetch;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.login.CredentialType;
import com.plociennik.vestal.login.CredentialsStorage;
import com.plociennik.vestal.login.KeyringCredentialsStorage;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GithubRepositoryFetcher {
    private CredentialsStorage credentialsStorage;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GithubRepositoryFetcher() {
        this.credentialsStorage = new KeyringCredentialsStorage();
    }

    public List<GitFetchRepository> fetchAllRepositories() throws IOException, InterruptedException {
        String token = credentialsStorage.get(CredentialType.GITHUB_TOKEN);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/user/repos?per_page=100"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("[{}] GitHub API returned status {}, body: {}", "0812_050826",
                    response.statusCode(), response.body());
            throw new VestalException("0812_050826", "GitHub API returned status %s, body: %s".formatted(
                    response.statusCode(), response.body()));
        }

        List<GitFetchRepository> repositories = new ArrayList<>();
        for (JsonNode node : objectMapper.readTree(response.body())) {
            repositories.add(new GitFetchRepository(node.get("full_name").asString(), node.get("clone_url").asString()));
        }
        return repositories;
    }
}
