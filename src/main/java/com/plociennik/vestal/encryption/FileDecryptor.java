package com.plociennik.vestal.encryption;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.git.util.FilesUtils;
import com.plociennik.vestal.git.util.StringUtils;
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
import java.util.Map;

@Slf4j
public class FileDecryptor {
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    DecryptResult decryptFile(Path fileToDecrypt, Path destination, SecretKey contentKeyContent, SecretKey contentKeyName, Map<String, String> foldersNamesDecryptionsMap) {
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

        byte[] plaintext = decryptContent(ciphertext, contentKeyContent, iv);

        ByteBuffer buffer = ByteBuffer.wrap(plaintext);
        int nameLength = buffer.getInt();
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        byte[] contentBytes = new byte[buffer.remaining()];
        buffer.get(contentBytes);

        String originalName = new String(nameBytes, StandardCharsets.UTF_8);
        Arrays.fill(plaintext, (byte) 0);

        Path destinationPath = decryptDestinationPath(originalName, destination, fileToDecrypt.toString(), contentKeyName, foldersNamesDecryptionsMap);
        return new DecryptResult(destinationPath, contentBytes, originalName);
    }

    private byte[] decryptContent(byte[] ciphertext, SecretKey key, byte[] iv) {
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

    private Path decryptDestinationPath(String originalName, Path destinationPath, String encryptedPath, SecretKey key, Map<String, String> foldersNamesDecryptionsMap) {
        AppConfig currentConfig = AppConfigManager.getInstance().getCurrentConfig();
        String rootFolder = currentConfig.vestalRepository.localRepository.rootFolder;

        int indexOfRootFolder = StringUtils.indexOf(rootFolder, encryptedPath);
        String pathToBeDecrypted = encryptedPath.substring(indexOfRootFolder + rootFolder.length() + 1);

        String separator = FilesUtils.getOperatingSystemFolderSeparator();
        String[] foldersWithFilename = pathToBeDecrypted.split(separator);
        if (foldersWithFilename.length <= 1) {
            return Path.of(originalName);
        }

        String[] folders = Arrays.copyOfRange(foldersWithFilename, 0, foldersWithFilename.length - 1);

        StringBuilder decryptedPath = new StringBuilder();
        for (String folder : folders) {
            String mapValue = foldersNamesDecryptionsMap.get(folder);
            String decryptResult;
            if (mapValue == null) {
                decryptResult = EncryptionUtils.decrypt(folder, key);
                foldersNamesDecryptionsMap.put(folder, decryptResult);
            } else {
                decryptResult = mapValue;
            }
            decryptedPath.append(separator).append(decryptResult);
        }
        decryptedPath.append(separator).append(originalName);
        return destinationPath.resolve(Path.of(decryptedPath.toString()));
    }
}
