package com.plociennik.vestal.git.push;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.git.util.FilesCollector;
import com.plociennik.vestal.git.util.GitUtils;
import com.plociennik.vestal.security.CredentialType;
import com.plociennik.vestal.security.CredentialsStorage;
import com.plociennik.vestal.security.KeyringCredentialsStorage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

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
            LocalRepository localRepository = currentConfig.vestalRepository.localRepository;
            List<Path> files = FilesCollector.from(Path.of(localRepository.encryptionPath));
            AddCommand add = git.add();

            Path repoRoot = gitRepository.getWorkTree().toPath();

            files.stream()
                    .map(file -> repoRoot.relativize(file).toString().replace(File.separatorChar, '/'))
                    .forEach(add::addFilepattern);
            add.call();

            // todo: status here is useless: due to non-deterministic way of encrypting files the result will always be
            // todo: different, thus status will never be clean
            Status status = git.status().call();
            if (status.isClean()) {
                log.info("[{}] There were no changes, cancelling the process.", "1232_10082026");
                return;
            }

            String customCommitMessage = createCustomCommitMessage();
            RevCommit commit = git.commit()
                    .setMessage(customCommitMessage)
                    .call();

            CredentialsProvider credentialsProvider =
                    new UsernamePasswordCredentialsProvider(credentialsStorage.get(CredentialType.GITHUB_TOKEN), "");

            // todo: push result has to be handled, otherwise can silently fail without logging anything
            Iterable<PushResult> pushResults = git.push()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        } catch (Exception e) {
            throw new VestalException("1218_10082026", "Something happened when trying to push changes.", e);
        }
        log.info("[{}] Push successful.", "1225_10082026");
    }

    private String createCustomCommitMessage() {
        LocalDateTime now = LocalDateTime.now();
        return now.toString();
    }
}
