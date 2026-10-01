package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiscUtils {

    public static void sleep(int seconds) {
        try {
            int durationInMillis = 1_000 * seconds;
            Thread.sleep(durationInMillis);
            log.info("[{}] Sleeping for [{}] seconds.", "0849_30092026", seconds);
        } catch (InterruptedException e) {
            throw new VestalException("0850_30092026", "Something happened while trying to sleep the thread.", e);
        }
    }
}
