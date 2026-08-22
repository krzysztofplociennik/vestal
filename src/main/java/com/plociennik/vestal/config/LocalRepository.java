package com.plociennik.vestal.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocalRepository {
    public String sourcePath;
    public String rootFolder;
    public String encryptionPath;

    public LocalRepository(String sourcePath) {
        this.sourcePath = sourcePath;
        this.rootFolder = extractRootFolder(sourcePath);
        this.encryptionPath = setupEncryptionPath();
    }

    // todo: logic in a POJO, weird

    private String setupEncryptionPath() {
        AppConfigManager appConfigManager = AppConfigManager.getInstance();
        AppConfig currentConfig = appConfigManager.getCurrentConfig();
        return currentConfig.operatingSystemMainDirectory.toString() + this.rootFolder;
    }

    private String extractRootFolder(String sourcePath) {
        final char SLASH = '/';
        String[] split = sourcePath.split("/");
        int length = split.length;
        if (length == 0) {
            // something wrong
            return SLASH + "dummy";
        } else {
            return SLASH + split[length - 1];
        }
    }
}
