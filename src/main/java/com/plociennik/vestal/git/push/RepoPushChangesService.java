package com.plociennik.vestal.git.push;

import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.git.util.GitUtils;
import com.plociennik.vestal.login.CredentialType;
import com.plociennik.vestal.login.CredentialsStorage;
import com.plociennik.vestal.login.KeyringCredentialsStorage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.plociennik.vestal.common.CommonUtils.logAndThrow;

@Slf4j
public class RepoPushChangesService {

    private AppConfigManager configManager = AppConfigManager.getInstance();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();

    public void push() {
        log.info("[{}] Pushing current state to remote.", "1222_10082026");

        // maybe check not needed, at least now
//        if (isRepositoryNotTextBased()) {
//            log.error("[{}] Repository consists of files different than .txt files, operation aborted.", "1341_09082026");
//            throw new VestalException("1341_09082026", "Repository consists of files different than .txt files, push aborted.");
//        }

        Repository gitRepository = GitUtils.getExistingLocalRepo();

        try (Git git = new Git(gitRepository)) {
            AppConfig currentConfig = configManager.getCurrentConfig();
            LocalRepository localRepository = currentConfig.localRepository;

            Path workDir = Paths.get(localRepository.path);
            List<String> txtFiles = findTxtFiles(workDir);

            if (txtFiles.isEmpty()) {
                log.info("[{}] There was nothing to stage because the local repo is empty.", "1231_10082026");
                return;
            }

            AddCommand add = git.add();
            txtFiles.forEach(add::addFilepattern);
            add.call();

            Status status = git.status().call();
            if (status.isClean()) {
                log.info("[{}] There were no changes, cancelling the process.", "1232_10082026");
                return;
            }

            git.commit()
                    .setMessage(createCustomCommitMessage())
                    .call();

            CredentialsProvider credentialsProvider =
                    new UsernamePasswordCredentialsProvider(credentialsStorage.get(CredentialType.GITHUB_TOKEN), "");

            // todo: push result for logging maybe
            git.push()
                    .setCredentialsProvider(credentialsProvider)
                    .call();

        } catch (GitAPIException | IOException e) {
            logAndThrow("1218_10082026", "Something happened when trying to push changes.", e);
        }
        log.info("[{}] Push successful.", "1225_10082026");
    }

    private List<String> findTxtFiles(Path workDir) throws IOException {
        List<String> txtFiles = new ArrayList<>();

        Files.walkFileTree(workDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName() != null && dir.getFileName().toString().equals(".git")) {
                    return FileVisitResult.SKIP_SUBTREE; // don't descend into .git at all
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                log.info("Visiting: {}", file);
                txtFiles.add(workDir.relativize(file).toString().replace(File.separatorChar, '/'));
                return FileVisitResult.CONTINUE;
            }
        });

        return txtFiles;
    }

    private String createCustomCommitMessage() {
        LocalDateTime now = LocalDateTime.now();
        return now.toString();
    }
}
