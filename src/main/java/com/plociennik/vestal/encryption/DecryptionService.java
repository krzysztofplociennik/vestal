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
public class DecryptionService {
    public List<DecryptResult> decryptPath(Path encryptionPath, Path destination) {
        log.info("[{}] Starting decryption process.", "1058_03092026");
        CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();
        String secretAsString = credentialsStorage.get(CredentialType.ENCRYPTION_SECRET_KEY);
        AppConfig currentConfig = AppConfigManager.getInstance().getCurrentConfig();
        byte[] encryptionSalt = currentConfig.getEncryptionSalt();
        SecretKey secretKeyContent = KeyDerivation.deriveKey(secretAsString.toCharArray(), encryptionSalt, KeyDerivation.PURPOSE_CONTENT);
        SecretKey secretKeyName = KeyDerivation.deriveKey(secretAsString.toCharArray(), encryptionSalt, KeyDerivation.PURPOSE_FILENAME);

        List<Path> filesToDecrypt = FilesUtils.collectFrom(encryptionPath).stream()
                .filter(p -> p.toString().endsWith(".enc"))
                .toList();

        FileDecryptor fileDecryptor = new FileDecryptor();
        List<DecryptResult> results = new ArrayList<>();

        Map<String, String> foldersNamesDecryptionsMap = new HashMap<>();

        for (Path file : filesToDecrypt) {
            log.info("[{}] Decrypting a file of a path: [{}].", "1100_03092026", file.toString());
            DecryptResult decryptResult = fileDecryptor.decryptFile(file, destination, secretKeyContent, secretKeyName, foldersNamesDecryptionsMap);
            results.add(decryptResult);
        }

        log.info("[{}] Decryption process finished successfully.", "1101_03092026");
        return results;
    }
}
