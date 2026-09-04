package com.plociennik.vestal.git.pull;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.encryption.DecryptionService;
import com.plociennik.vestal.encryption.FileDecryptor;
import com.plociennik.vestal.git.status.GitStatusService;
import com.plociennik.vestal.git.util.FilesUtils;
import com.plociennik.vestal.git.util.GitUtils;
import com.plociennik.vestal.security.CredentialType;
import com.plociennik.vestal.security.CredentialsStorage;
import com.plociennik.vestal.security.KeyringCredentialsStorage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class RepoPullChangesService {

    private AppConfigManager appConfigManager = AppConfigManager.getInstance();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();
    private DecryptionService decryptionService = new DecryptionService();
    private GitStatusService gitStatusService = new GitStatusService();

    public void pull() {
        log.info("[{}] Starting the pull process.", "1208_03092026");
        fetchFiles();
        List<FileDecryptor.DecryptResult> decryptResults = decryptFiles();
        if (decryptResults.isEmpty()) {
            log.info("[{}] Remote repository is empty, cancelling the process.", "1441_04092026");
            return;
        }
        Path sourcePath = Path.of(appConfigManager.getCurrentConfig().vestalRepository.localRepository.sourcePath);
        // todo: potentially, there could always be a backup stored somewhere, limit to a specific number, like 3 or 4
        deleteOldFiles(sourcePath);
        pasteNewFiles(decryptResults, sourcePath);
        gitStatusService.updateManifest();
        log.info("[{}] Pull process successful.", "1209_03092026");
    }

    private void fetchFiles() {
        log.info("[{}] Fetching files from the remote repository.", "1210_03092026");
        File repoDirectory = GitUtils.getExistingLocalRepo().getDirectory();

        String githubToken = credentialsStorage.get(CredentialType.GITHUB_TOKEN);
        String branchName = "master";

        try (Git git = Git.open(repoDirectory)) {
            git.fetch()
                    .setRemote("origin")
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                    .call();

            Repository repo = git.getRepository();
            ObjectId remoteHead = repo.resolve("refs/remotes/origin/" + branchName);

            if (remoteHead == null) {
                throw new VestalException(
                        "1324_04092026",
                        "Could not resolve origin/%s after fetch. Check the branch name is correct.".formatted(branchName)
                );
            }

            git.reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .setRef(remoteHead.getName())
                    .call();

        } catch (IOException | GitAPIException e) {
            throw new VestalException("1323_04092026", "Something happened while trying to fetch remote files.", e);
        }
        log.info("[{}] Fetching process successful.", "1211_03092026");
    }

    private List<FileDecryptor.DecryptResult> decryptFiles() {
        AppConfig currentConfig = appConfigManager.getCurrentConfig();
        Path encryptionPath = Path.of(currentConfig.vestalRepository.localRepository.encryptionPath);
        Path sourcePath = Path.of(currentConfig.vestalRepository.localRepository.sourcePath);
        return decryptionService.decryptPath(encryptionPath, sourcePath);
    }

    private void deleteOldFiles(Path path) {
        log.info("[{}] Deleting old files.", "1212_03092026");
        List<Path> files = FilesUtils.collectFrom(path);
        for (Path file : files) {
            FilesUtils.delete(file);
        }
        log.info("[{}] Deleting old files was successful.", "1213_03092026");
    }

    private void pasteNewFiles(List<FileDecryptor.DecryptResult> decryptResults, Path destination) {
        String separator = FilesUtils.getOperatingSystemFolderSeparator();
        decryptResults.forEach(r -> {
            Path fullPath = Path.of(destination.toString() + separator + r.originalName());
            FilesUtils.write(fullPath, r.content());
        });
    }
}
