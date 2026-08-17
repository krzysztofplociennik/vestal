package com.plociennik.vestal.config;

import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Slf4j
public class MainDirectoryInitService {

    private final AppConfigManager appConfigManager = AppConfigManager.getInstance();

    public void init() {
        OperatingSystem os = establishOs();
        Path establishedDirectory = establishRepositoriesDirectory(os);
        if (isMainDirectoryPresent(establishedDirectory)) {
            log.info("[{}] Main directory is present.", "1625_13082026");
            return;
        }

        AppConfig currentConfig = appConfigManager.getCurrentConfig();
        currentConfig.operatingSystemMainDirectory = establishedDirectory;
        appConfigManager.saveConfig(currentConfig);

        try {
            Files.createDirectories(establishedDirectory);
        } catch (IOException e) {
            throw new VestalException("1628_13082026", "Could not create directory of [%s].".formatted(establishedDirectory), e);
        }
        log.info("[{}] Main directory has been created successfully, path: [{}].", "1630_13082026", establishedDirectory);
    }

    private boolean isMainDirectoryPresent(Path path) {
        File mainPath = new File(path.toString());
        return mainPath.exists();
    }

    private Path establishRepositoriesDirectory(OperatingSystem operatingSystem) {
        return switch (operatingSystem) {
            case WINDOWS -> Paths.get(
                    System.getenv("LOCALAPPDATA"),
                    "vestal repositories"
            );
            case LINUX -> Paths.get(
                    System.getProperty("user.home"),
                    ".local",
                    "share",
                    "vestal repositories"
            );
        };
    }

    private OperatingSystem establishOs() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            return OperatingSystem.WINDOWS;
        }

        if (os.contains("linux")) {
            return OperatingSystem.LINUX;
        }
        throw new VestalException("1452_13082026", "Unsupported operating system of [%s]".formatted(os));
    }

    private enum OperatingSystem {
        WINDOWS,
        LINUX
    }
}
