package com.plociennik.vestal.encryption;

import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Slf4j
public class FileDecryptor {
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    DecryptResult decryptFile(Path fileToDecrypt, Path destination, SecretKey contentKey) {
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(fileToDecrypt);
        } catch (IOException e) {
            throw new VestalException("1102_03092026", "Something happened while trying to read an encrypted file.", e);
        }

        if (fileBytes.length < IV_LENGTH_BYTES) {
            throw new VestalException("1103_03092026",
                    "Encrypted file is too short to contain a valid IV: " + fileToDecrypt);
        }

        byte[] iv = Arrays.copyOfRange(fileBytes, 0, IV_LENGTH_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(fileBytes, IV_LENGTH_BYTES, fileBytes.length);

        byte[] plaintext = decrypt(ciphertext, contentKey, iv);

        ByteBuffer buffer = ByteBuffer.wrap(plaintext);
        int nameLength = buffer.getInt();
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        byte[] contentBytes = new byte[buffer.remaining()];
        buffer.get(contentBytes);

        String originalName = new String(nameBytes, StandardCharsets.UTF_8);
        Arrays.fill(plaintext, (byte) 0);

        Path destinationFile = destination.resolve(originalName);
        return new DecryptResult(destinationFile, contentBytes, originalName);
    }

    private byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (AEADBadTagException e) {
            throw new VestalException("1104_03092026",
                    "Failed to decrypt file - authentication check failed (wrong key or corrupted file): " + ciphertext.length, e);
        } catch (GeneralSecurityException e) {
            throw new VestalException("1105_03092026", "Something happened while trying to decrypt a file.", e);
        }
    }

    public record DecryptResult(Path destinationFile, byte[] content, String originalName) {}
}
