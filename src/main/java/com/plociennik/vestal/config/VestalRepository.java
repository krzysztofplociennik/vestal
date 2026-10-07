package com.plociennik.vestal.config;

import com.plociennik.vestal.state.State;
import lombok.Data;

@Data
public class VestalRepository {
    public LocalRepository localRepository;
    public RemoteRepository remoteRepository;
    public State state;

    public VestalRepository() {
        this.localRepository = new LocalRepository();
        this.remoteRepository = new RemoteRepository();
    }

    public void clear() {
        this.localRepository.clear();
        this.remoteRepository.clear();
    }
}
