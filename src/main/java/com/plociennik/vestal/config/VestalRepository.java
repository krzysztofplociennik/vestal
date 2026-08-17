package com.plociennik.vestal.config;

import lombok.Data;

@Data
public class VestalRepository {
    public LocalRepository localRepository;
    public RemoteRepository remoteRepository;

    public VestalRepository() {
        this.localRepository = new LocalRepository();
        this.remoteRepository = new RemoteRepository();
    }
}
