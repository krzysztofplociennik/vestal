package com.plociennik.vestal.encryption;

import com.plociennik.vestal.common.VestalException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public final class KeyDerivation {
    private static final int KEY_LENGTH_BITS = 256;
    private static final int ITERATIONS = 210_000;

    public static final String PURPOSE_CONTENT = "content";
    public static final String PURPOSE_FILENAME = "filename";

    private KeyDerivation() {}

    public static SecretKey deriveKey(char[] password, byte[] salt, String purpose) {
        byte[] domainSalt = concat(salt, purpose.getBytes(StandardCharsets.UTF_8));
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new VestalException("1358_04092026", "Algorithm has not been found.", e);
        }
        PBEKeySpec spec = new PBEKeySpec(password, domainSalt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            try {
                return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            } catch (InvalidKeySpecException e) {
                throw new VestalException("1359_04092026", "Something happened while trying to create a secret key specification.", e);
            }
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
