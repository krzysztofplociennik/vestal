package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class GitUtils {

    private static AppConfigManager configManager = AppConfigManager.getInstance();

    public static Repository getExistingLocalRepo() {
        AppConfig currentConfig = configManager.getCurrentConfig();
        String pathAsString = currentConfig.vestalRepository.localRepository.sourcePath;
        Path path = Path.of(pathAsString);
        File gitDir = new File(path.toFile(), ".git");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            return builder.setGitDir(gitDir)
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            log.error("[{}] Something happened when trying to retrieve existing local repository, error: [{}].", "1118_10082026", e.toString());
            throw new VestalException("1118_10082026", "Something happened when trying to retrieve existing local repository.", e);
        }
    }
}
