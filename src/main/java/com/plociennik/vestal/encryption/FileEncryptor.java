package com.plociennik.vestal.encryption;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.git.util.FilesUtils;
import com.plociennik.vestal.git.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

// todo: tests would be nice, to make sure it works
// todo: maybe a refactor

public class FileEncryptor {
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String SEPARATOR = FilesUtils.getOperatingSystemFolderSeparator();

    EncryptResult encryptFile(Path fileToEncrypt, Path destination,
                              SecretKey contentKey, SecretKey nameKey, Map<String, String> folderNormalNamesAndEncryptedNames) {

        // encrypt output
        String originalName = fileToEncrypt.getFileName().toString();
        byte[] nameBytes = originalName.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes;
        try {
            contentBytes = Files.readAllBytes(fileToEncrypt);
        } catch (IOException e) {
            throw new VestalException("1343_22082026", "Something happened while trying to read contents of a file.", e);
        }

        ByteBuffer buffer = ByteBuffer.allocate(4 + nameBytes.length + contentBytes.length);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        buffer.put(contentBytes);
        byte[] plaintext = buffer.array();

        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        byte[] ciphertext = encryptContent(plaintext, contentKey, iv);
        Arrays.fill(plaintext, (byte) 0);

        byte[] fileOutput = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, fileOutput, 0, iv.length);
        System.arraycopy(ciphertext, 0, fileOutput, iv.length, ciphertext.length);

        // encrypt folder names on the source path
        String[] fileFoldersOnPath = splitFolders(fileToEncrypt);
        String[] fileFolders = encryptFolderNames(fileFoldersOnPath, nameKey, folderNormalNamesAndEncryptedNames);

        // encrypt the file destination with folders and the name itself
        StringBuilder encryptedFoldersOnPath = new StringBuilder(destination.toString());
        for (String fileEncryptedFoldersName : fileFolders) {
            encryptedFoldersOnPath.append(SEPARATOR).append(fileEncryptedFoldersName);
        }

        Path pathWithEncryptedFolders = Path.of(encryptedFoldersOnPath.toString());
        String encryptedFilename = EncryptionUtils.encrypt(originalName, nameKey) + ".enc";
        Path fileDestination = pathWithEncryptedFolders.resolve(encryptedFilename);

        return new EncryptResult(fileDestination, fileFolders, fileOutput);
    }

    private String[] splitFolders(Path file) {
        String filePath = file.toString();

        AppConfigManager appConfigManager = AppConfigManager.getInstance();
        AppConfig currentConfig = appConfigManager.getCurrentConfig();
        String rootFolder = currentConfig.vestalRepository.localRepository.rootFolder;

        int rootFolderIndex = StringUtils.indexOf(rootFolder, filePath);
        String substring = filePath.substring(rootFolderIndex + rootFolder.length() + 1);

        String[] folders = substring.split(SEPARATOR);
        if (folders.length == 1) {
            return new String[0];
        }
        return Arrays.copyOfRange(folders, 0, folders.length - 1);
    }

    private String[] encryptFolderNames(String[] originalFolders, SecretKey key, Map<String, String> folderNormalNamesAndEncryptedNames) {
        if (originalFolders.length == 0) {
            return new String[0];
        }
        String[] encryptedFoldersNames = new String[originalFolders.length];
        for (int i = 0; i < originalFolders.length; i++) {
            String originalFolderName = originalFolders[i];
            String possibleEncryptedName = folderNormalNamesAndEncryptedNames.get(originalFolderName);
            if (possibleEncryptedName == null) {
                String encryptedFolderName = EncryptionUtils.encrypt(originalFolderName, key);
                encryptedFoldersNames[i] = encryptedFolderName;
                folderNormalNamesAndEncryptedNames.put(originalFolderName, encryptedFolderName);
            } else {
                encryptedFoldersNames[i] = possibleEncryptedName;
            }
        }
        return encryptedFoldersNames;
    }

    private byte[] encryptContent(byte[] plaintext, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(plaintext);
        } catch (GeneralSecurityException e) {
            throw new VestalException("1312_18082026", "Something happened while trying to encrypt a file.", e);
        }
    }
}
