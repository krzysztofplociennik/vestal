package com.plociennik.vestal.encryption;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

// todo: pure LLM, review, refactor

@Slf4j
public class FileEncryptor {
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    public EncryptResult encryptFile(Path fileToEncrypt, Path destination, SecretKey secretKey) {
        byte[] plaintext;
        try {
            plaintext = Files.readAllBytes(fileToEncrypt);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + fileToEncrypt, e);
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);

        byte[] ciphertext = encrypt(plaintext, secretKey, iv);
        Arrays.fill(plaintext, (byte) 0); // done with it, wipe it

        byte[] output = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(ciphertext, 0, output, iv.length, ciphertext.length);

        String name = fileToEncrypt.getFileName().toString();
        Path destFile = destination.resolve("[155817082026]" + name + ".enc");

        return new EncryptResult(destFile, output);
    }

    private byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(plaintext);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Encryption failed for one file", e);
        }
    }

    public record EncryptResult(Path destFile, byte[] output) {}
}
