package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;

public class StringUtils {

    public static int indexOf(String substring, String string) {
        int i = string.indexOf(substring);
        if (i == -1) {
            throw new VestalException(
                    "1024_15092026",
                    "The substring of [%s] has not been found in the string of [%s]".formatted(substring, string));
        }
        return i;
    }

    public static int indexOf(char substring, String string) {
        return indexOf(String.valueOf(substring), string);
    }

    public static int indexOf(String substring, String string, int start) {
        String startSubstring = string.substring(start);
        return indexOf(substring, startSubstring);
    }

    public static int indexOf(char substring, String string, int start) {
        return indexOf(String.valueOf(substring), string, start);
    }
}
