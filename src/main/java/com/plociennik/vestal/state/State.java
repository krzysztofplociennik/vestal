package com.plociennik.vestal.state;

import lombok.Getter;

public enum State {
    LOGGED_OUT(1),
    LOGIN(2),
    ENCRYPTION_KEY(3),
    REPOS(4),
    ACTIONS(5);

    @Getter
    private final int order;

    State(int order) {
        this.order = order;
    }
}
