package com.plociennik.vestal.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VestalException extends RuntimeException {

    @Getter private final String errorID;

    public VestalException(String errorID, String message) {
        super("(%s) %s".formatted(errorID, message));
        this.errorID = errorID;
        log.error("[{}] {}", errorID, message);
    }

    public VestalException(String errorID, String message, Exception e) {
        super("(%s) %s\nCause: [%s]".formatted(errorID, message, e.toString()));
        this.errorID = errorID;
        log.error("[{}] {}", errorID, message, e);
    }
}
