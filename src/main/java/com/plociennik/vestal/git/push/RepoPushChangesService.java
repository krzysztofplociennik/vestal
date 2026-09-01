package com.plociennik.vestal.git.push;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.git.status.GitStatusService;
import com.plociennik.vestal.git.util.FilesUtils;
import com.plociennik.vestal.git.util.GitUtils;
import com.plociennik.vestal.security.CredentialType;
import com.plociennik.vestal.security.CredentialsStorage;
import com.plociennik.vestal.security.KeyringCredentialsStorage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RepoPushChangesService {

    private AppConfigManager configManager = AppConfigManager.getInstance();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();
    private GitStatusService gitStatusService = new GitStatusService();

    public void push() {
        log.info("[{}] Checking if there are any changes that warrant a push.", "1130_01092026");
        boolean isClean = gitStatusService.isClean();
        if (isClean) {
            log.info("[{}] There were no changes, cancelling the process.", "1232_10082026");
            return;
        }
        log.info("[{}] There are changes, pushing current state to remote.", "1222_10082026");

        Repository gitRepository = GitUtils.getExistingLocalRepo();
        try (Git git = new Git(gitRepository)) {
            AppConfig currentConfig = configManager.getCurrentConfig();
            LocalRepository localRepository = currentConfig.vestalRepository.localRepository;
            removeExistingFilesFromTracking(git, gitRepository);
            addNewFilesToTracking(git, gitRepository, localRepository);
            commitFiles(git);
            Iterable<PushResult> pushResults = pushFiles(git);
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
        gitStatusService.updateManifest();
    }

    private void removeExistingFilesFromTracking(Git git, Repository gitRepository) {
        log.info("[{}] Untracking all existing files.", "1252_01092026");
        DirCache dirCache = null;
        try {
            dirCache = gitRepository.readDirCache();
        } catch (IOException e) {
            throw new VestalException("1250_01092026", "Something happened when trying to read local repository.", e);
        }
        List<String> trackedPaths = new ArrayList<>();
        for (int i = 0; i < dirCache.getEntryCount(); i++) {
            String path = dirCache.getEntry(i).getPathString();
            trackedPaths.add(path);
        }
        if (!trackedPaths.isEmpty()) {
            RmCommand rm = git.rm().setCached(true);
            trackedPaths.forEach(rm::addFilepattern);
            try {
                rm.call();
            } catch (GitAPIException e) {
                throw new VestalException("1251_01092026", "Something happened when trying to untrack files.", e);
            }
        }
        log.info("[{}] Untracking successful.", "1254_01092026");

    }

    private void addNewFilesToTracking(Git git, Repository gitRepository, LocalRepository localRepository ) {
        List<Path> files = FilesUtils.collectFrom((Path.of(localRepository.encryptionPath)));
        AddCommand add = git.add();

        Path repoRoot = gitRepository.getWorkTree().toPath();

        files.stream()
                .map(file -> repoRoot.relativize(file).toString().replace(File.separatorChar, '/'))
                .forEach(add::addFilepattern);
        try {
            add.call();
        } catch (GitAPIException e) {
            throw new VestalException("1257_01092026", "Something happened when trying to add new files to tracking.", e);
        }
    }

    private void commitFiles(Git git) {
        try {
            git.commit()
                    .setMessage(LocalDateTime.now().toString())
                    .call();
        } catch (GitAPIException e) {
            throw new VestalException("1301_01092026", "Something happened when trying to commit files.", e);
        }
    }

    private Iterable<PushResult> pushFiles(Git git) {
        final String githubToken = credentialsStorage.get(CredentialType.GITHUB_TOKEN);
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubToken, "");
        try {
            return git
                    .push()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        } catch (GitAPIException e) {
            throw new VestalException("1302_0109206", "Something happened when trying to push files.", e);
        }
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
