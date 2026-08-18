package com.plociennik.vestal.encryption;

import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

// todo: pure LLM, review, refactor
// todo: a test would be nice, to make sure it works

@Slf4j
public class FileEncryptor {
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    public EncryptResult encryptFile(Path fileToEncrypt, Path destination,
                                     SecretKey contentKey, SecretKey nameKey) throws IOException {
        String originalName = fileToEncrypt.getFileName().toString();
        byte[] nameBytes = originalName.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = Files.readAllBytes(fileToEncrypt);

        ByteBuffer buffer = ByteBuffer.allocate(4 + nameBytes.length + contentBytes.length);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        buffer.put(contentBytes);
        byte[] plaintext = buffer.array();

        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        byte[] ciphertext = encrypt(plaintext, contentKey, iv);
        Arrays.fill(plaintext, (byte) 0);

        byte[] output = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(ciphertext, 0, output, iv.length, ciphertext.length);

        String encryptedName = deriveFileName(originalName, nameKey);
        Path destinationFile = destination.resolve(encryptedName);

        return new EncryptResult(destinationFile, output);
    }

    private byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(plaintext);
        } catch (GeneralSecurityException e) {
            throw new VestalException("1312_18082026", "Something happened while trying to encrypt a file.", e);
        }
    }

    private String deriveFileName(String originalName, SecretKey nameKey) {
        try {
            final String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(nameKey.getEncoded(), algorithm));
            byte[] digest = mac.doFinal(originalName.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest) + ".enc";
        } catch (GeneralSecurityException e) {
            throw new VestalException("1338_18082026", "Something happened while deriving filename.", e);
        }
    }

    public record EncryptResult(Path destFile, byte[] output) {}
}
