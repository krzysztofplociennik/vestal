package com.plociennik.vestal.config;

import lombok.Data;

@Data
public class AppConfig {

    public Directory directory;
    public Repository repository;

    public AppConfig() {
        this.directory = new Directory();
        this.repository = new Repository();
    }
}
