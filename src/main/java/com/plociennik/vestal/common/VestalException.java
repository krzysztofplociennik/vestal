package com.plociennik.vestal.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VestalException extends RuntimeException {

    public VestalException(String errorID, String message) {
        super("(%s) %s".formatted(errorID, message));
        log.error("[{}] {}", errorID, message);
    }

    public VestalException(String errorID, String message, Exception e) {
        super("(%s) %s\nCause: [%s]".formatted(errorID, message, e.toString()));
        log.error("[{}] {}", errorID, message, e);
    }
}
