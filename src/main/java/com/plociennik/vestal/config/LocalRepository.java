package com.plociennik.vestal.config;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.git.util.FilesUtils;
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
        String separator = FilesUtils.getOperatingSystemFolderSeparator();
        String[] split = sourcePath.split("/");
        int length = split.length;
        if (length == 0) {
            throw new VestalException(
                    "1138_07092026",
                    "There is something wrong with the source path: [%s].". formatted(sourcePath));
        } else {
            return separator + split[length - 1];
        }
    }
}
