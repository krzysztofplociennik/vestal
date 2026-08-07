package com.plociennik.vestal.config;

import lombok.Data;

@Data
public class AppConfig {

    public Directory directory;
    public GitHubRepository gitHubRepository;

    public AppConfig() {
        this.directory = new Directory();
        this.gitHubRepository = new GitHubRepository();
    }
}
