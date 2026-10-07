package com.plociennik.vestal.config;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.encryption.SaltGenerator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

@Slf4j
public class AppConfigManager {

    private final ObjectMapper mapper = new ObjectMapper();
    private final static String CONFIG_FILE_NAME = "config.json";
    private File configFile = null;

    private final ReadOnlyObjectWrapper<AppConfig> currentConfig = new ReadOnlyObjectWrapper<>();

    private AppConfigManager() {
        createConfigFileIfAbsent();
        currentConfig.set(readFromDisk());
        generateSaltIfMissing();
    }

    public AppConfig getCurrentConfig() {
        return getCurrentConfigProperty().getValue();
    }

    public ReadOnlyProperty<AppConfig> getCurrentConfigProperty() {
        return this.currentConfig.getReadOnlyProperty();
    }

    private AppConfig readFromDisk() {
        return mapper.readValue(configFile, AppConfig.class);
    }

    private void writeToDisk(AppConfig updatedConfig) {
        mapper.writeValue(configFile.toPath().toFile(), updatedConfig);
    }

    public void update(Consumer<AppConfig> edit) {
        AppConfig draft = mapper.convertValue(currentConfig.get(), AppConfig.class);
        edit.accept(draft);
        writeToDisk(draft);
        currentConfig.set(draft);
    }

    private void createConfigFileIfAbsent() {
        boolean notExists = Files.notExists(Path.of(CONFIG_FILE_NAME));
        if (notExists) {
            log.info("[{}] No config file exists, I am creating one now...", "1409_020826");
            Path configFile = Path.of(CONFIG_FILE_NAME);
            try {
                Files.writeString(configFile, "{}", StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new VestalException("1411_020826", "Something happened while trying to create a config file.", e);
            }
            log.info("[{}] The file [{}] has been successfully created.", "1409_020826", CONFIG_FILE_NAME);
        }
        this.configFile = new File(CONFIG_FILE_NAME);
        log.info("[{}] The config file has been successfully loaded.", "1417_020826");
    }

    private void generateSaltIfMissing() {
        if (getCurrentConfig().encryptionSalt == null) {
            update(ac -> ac.encryptionSalt = SaltGenerator.generateSalt());
        }
    }

    public static AppConfigManager getInstance() {
        return AppConfigManagerHelper.INSTANCE;
    }

    private static class AppConfigManagerHelper {
        private static final AppConfigManager INSTANCE = new AppConfigManager();
    }
}
