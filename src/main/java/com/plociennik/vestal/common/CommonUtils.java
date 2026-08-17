package com.plociennik.vestal.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtils {

    // todo: logAndThrow won't make the compiler shout for returning a value if needed
    public static void logAndThrow(String eid, String message) {
        log.error("[{}] {}", eid, message);
        throw new VestalException(eid, message);
    }

    public static void logAndThrow(String eid, String message, Exception e) {
        log.error("[{}] {}", eid, message, e);
        throw new VestalException(eid, message, e);
    }
}
