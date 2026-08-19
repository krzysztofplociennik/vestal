package com.plociennik.vestal.config;

import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Slf4j
public class MainDirectoryService {

    private final AppConfigManager appConfigManager = AppConfigManager.getInstance();

    public void init() {
        log.info("[{}] Checking if the main directory is present.", "1448_18082026");

        AppConfig currentConfig = appConfigManager.getCurrentConfig();
        if (currentConfig.operatingSystemMainDirectory != null) {
            log.info("[{}] Main directory is present.", "1625_13082026");
            return;
        }

        log.info("[{}] Main directory is not present, creating it now.", "1449_18082026");
        OperatingSystem os = establishOs();
        Path establishedDirectory = establishMainDirectoryPath(os);
        currentConfig.operatingSystemMainDirectory = establishedDirectory;
        appConfigManager.saveConfig(currentConfig);

        try {
            Files.createDirectories(establishedDirectory);
        } catch (IOException e) {
            throw new VestalException("1628_13082026", "Could not create directory of [%s].".formatted(establishedDirectory), e);
        }
        log.info("[{}] Main directory has been created successfully, path: [{}].", "1630_13082026", establishedDirectory);
    }

    private Path establishMainDirectoryPath(OperatingSystem operatingSystem) {
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
