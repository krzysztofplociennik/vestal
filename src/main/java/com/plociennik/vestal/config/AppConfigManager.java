package com.plociennik.vestal.config;

import com.plociennik.vestal.common.VestalException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class AppConfigManager {

    private final static String CONFIG_FILE_NAME = "config.json";
    private File configFile = null;
    private final ObjectMapper mapper;
    @Getter private AppConfig currentConfig;

    public AppConfigManager() {
        createConfigFileIfAbsent();
        mapper = new ObjectMapper();
        currentConfig = loadCurrentConfig();
    }

    public void saveDirectoryPath(String path) {
        AppConfig appConfig = loadCurrentConfig();
        appConfig.directory.path = path;
        mapper.writeValue(configFile.toPath().toFile(), appConfig);
    }

    public void saveRepositoryNameAndUrl(String name, String url) {
        AppConfig appConfig = loadCurrentConfig();
        appConfig.gitHubRepository.name = name;
        appConfig.gitHubRepository.url = url;
        mapper.writeValue(configFile.toPath().toFile(), appConfig);
    }

    private void createConfigFileIfAbsent() {
        boolean notExists = Files.notExists(Path.of(CONFIG_FILE_NAME));
        if (notExists) {
            log.info("[{}] No config file exists, I am creating one now...", "1409_020826");
            Path configFile = Path.of("config.json");
            try {
                Files.writeString(configFile, "{}", StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.warn("[{}] Something happened while trying to create a config file. Error: [{}]", "1411_020826", e.toString());
                throw new VestalException("1411_020826", "Something happened while trying to create a config file.", e);
            }
            log.info("[{}] The file [{}] has been successfully created.", "1409_020826", CONFIG_FILE_NAME);
        }
        this.configFile = new File(CONFIG_FILE_NAME);
        log.info("[{}] The config file has been successfully loaded.", "1417_020826");
    }

    private AppConfig loadCurrentConfig() {
        return mapper.readValue(configFile, AppConfig.class);
    }
}
