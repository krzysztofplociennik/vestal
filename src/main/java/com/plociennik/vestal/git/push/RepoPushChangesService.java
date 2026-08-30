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
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RepoPushChangesService {

    private AppConfigManager configManager = AppConfigManager.getInstance();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();

    public void push() {
        log.info("[{}] Pushing current state to remote.", "1222_10082026");

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
            git.commit()
                    .setMessage(customCommitMessage)
                    .call();

            CredentialsProvider credentialsProvider =
                    new UsernamePasswordCredentialsProvider(credentialsStorage.get(CredentialType.GITHUB_TOKEN), "");

            Iterable<PushResult> pushResults = git
                    .push()
                    .setCredentialsProvider(credentialsProvider)
                    .call();

            PushSummary pushSummary = processResults(pushResults, currentConfig.vestalRepository.remoteRepository.url);
            // todo: maybe some of the statuses should be handled visibly
            if (!pushSummary.success) {
                log.error("[{}] Push not successful, reasons: [{}]", "1027_20082026", pushSummary.errorMessages);
                throw new VestalException("1027_20082026", "Push not successful, check logs.");
            }
        } catch (Exception e) {
            throw new VestalException("1218_10082026", "Something happened when trying to push changes.", e);
        }
        log.info("[{}] Push successful.", "1225_10082026");
    }

    private String createCustomCommitMessage() {
        LocalDateTime now = LocalDateTime.now();
        return now.toString();
    }

    private PushSummary processResults(Iterable<PushResult> pushResults, String remoteUrl) {
        if (pushResults == null) {
            return new PushSummary(false, "PushResults are null.", null);
        }

        PushResult pushResult = null;
        for (PushResult pr : pushResults) {
            String uri = pr.getURI().toString();
            if (remoteUrl.equals(uri)) {
                pushResult = pr;
            } else {
                log.warn("[{}] There are push results with a wrong URI of: [{}]", "1146_20082026", uri);
                return new PushSummary(
                        false,
                        "URIs do not match, expected: [%s], actual: [%s].".formatted(remoteUrl, uri),
                        new ArrayList<>());
            }
        }

        if (pushResult == null) {
            throw new VestalException("1356_20082026", "PushResult is null.");
        }

        List<PushResultError> pushResultErrors = new ArrayList<>();

        pushResult.getRemoteUpdates().stream()
                .filter(r -> !r.getStatus().equals(RemoteRefUpdate.Status.OK))
                .forEach(r -> pushResultErrors.add(new PushResultError(r.getStatus().toString(), r.getMessage())));

        if (pushResultErrors.isEmpty()) {
            return new PushSummary(true, "", null);
        }
        return new PushSummary(false, "There were problems with the push.", pushResultErrors);
    }

    private record PushSummary(boolean success, String reason, List<PushResultError> errorMessages) {}

    private record PushResultError(String status, String message) {}
}
