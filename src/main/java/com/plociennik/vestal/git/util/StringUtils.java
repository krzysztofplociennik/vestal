package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;

public class StringUtils {

    public static int indexOf(String substring, String string) {
        int i = string.indexOf(substring);
        if (i == -1) {
            throw new VestalException(
                    "1024_15092026",
                    "Substring of [%s] has not been found in the string of [%s]".formatted(substring, string));
        }
        return i;
    }
}
