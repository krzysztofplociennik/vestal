package com.plociennik.vestal.config;

import com.plociennik.vestal.state.State;
import lombok.Data;

@Data
public class VestalRepository {
    public LocalRepository localRepository;
    public RemoteRepository remoteRepository;
    public State currentState;

    public VestalRepository() {
        this.localRepository = new LocalRepository();
        this.remoteRepository = new RemoteRepository();
        this.currentState = State.LOGIN;
    }
}
