package com.plociennik.vestal.encryption;

import java.security.SecureRandom;

public class SaltGenerator {

    private static final int SALT_LENGTH_BYTES = 16;

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
