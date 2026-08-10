package com.plociennik.vestal.config;

import lombok.Data;

@Data
public class AppConfig {

    public LocalRepository localRepository;
    public RemoteRepository remoteRepository;

    public AppConfig() {
        this.localRepository = new LocalRepository();
        this.remoteRepository = new RemoteRepository();
    }
}
