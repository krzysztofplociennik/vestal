package com.plociennik.vestal.common;

public class VestalException extends RuntimeException {

    public VestalException(String errorID, String message) {
        super("(%s) %s".formatted(errorID, message));
    }

    public VestalException(String errorID, String message, Exception e) {
        super("(%s) %s\nCause: [%s]".formatted(errorID, message, e.toString()));
    }
}
