package com.plociennik.vestal.encryption;

import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.git.util.FilesUtils;
import com.plociennik.vestal.security.CredentialType;
import com.plociennik.vestal.security.CredentialsStorage;
import com.plociennik.vestal.security.KeyringCredentialsStorage;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class EncryptionService {

    public List<EncryptResult> encryptPath(Path source, Path destination) {
        log.info("[{}] Starting encryption process.", "1316_19082026");
        CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();
        String secretAsString = credentialsStorage.get(CredentialType.ENCRYPTION_SECRET_KEY);
        AppConfig currentConfig = AppConfigManager.getInstance().getCurrentConfig();
        byte[] encryptionSalt = currentConfig.getEncryptionSalt();

        SecretKey secretKeyContent = KeyDerivation.deriveKey(secretAsString.toCharArray(), encryptionSalt, KeyDerivation.PURPOSE_CONTENT);
        SecretKey secretKeyFilename = KeyDerivation.deriveKey(secretAsString.toCharArray(), encryptionSalt, KeyDerivation.PURPOSE_FILENAME);

        List<Path> filesToEncrypt = FilesUtils.collectFrom(source).stream()
                .filter(p -> !isHiddenFile(p))
                .toList();
        FileEncryptor fileEncryptor = new FileEncryptor();
        List<EncryptResult> results = new ArrayList<>();
        Map<String, String> folderNormalNamesAndEncryptedNames = new HashMap<>();
        for (Path file : filesToEncrypt) {
            log.info("[{}] Encrypting a file of a path: [{}].", "1456_24082026", file.toString());
            EncryptResult encryptResult = fileEncryptor.encryptFile(file, destination, secretKeyContent, secretKeyFilename, folderNormalNamesAndEncryptedNames);
            results.add(encryptResult);
        }
        log.info("[{}] Encryption process finished successfully.", "1318_19082026");
        return results;
    }

    private boolean isHiddenFile(Path path) {
        String pathAsString = path.toString();
        final String separator = FilesUtils.getOperatingSystemFolderSeparator();
        String[] split = pathAsString.split(separator);
        int length = split.length;
        String lastSplit = split[length - 1];
        return lastSplit.startsWith(".");
    }
}
