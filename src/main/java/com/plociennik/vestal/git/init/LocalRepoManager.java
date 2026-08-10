package com.plociennik.vestal.git.init;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.config.RemoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class LocalRepoManager {

    public void ensureLocalRepositoryInitialized(LocalRepository directory) {
        log.info("[{}] I am checking if the local git repository is present.", "1416_07082026");

        Path pathToLocalRepo = Path.of(directory.path);
        File gitDir = new File(pathToLocalRepo.toFile(), ".git");
        Repository repository;
        if (gitDir.exists()) {
            repository = getExistingLocalRepo(directory.path);
            log.info("[{}] Local repo already exists, identifier [{}].", "1115_10082026", repository.getIdentifier());
        } else {
            log.info("[{}] Local repo does not exist for the directory [{}], I am creating it now.", "1112_10082026", directory.path);
            repository = initRepo(pathToLocalRepo);
            log.info("[{}] Local repo of identifier [{}] has been created.", "1116_10082026", repository.getIdentifier());
        }
    }

    private Repository getExistingLocalRepo(String path) {
        File gitDir = new File(Path.of(path).toFile(), ".git");

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

    private Repository initRepo(Path directory) {
        try (Git git = Git.init()
                .setDirectory(directory.toFile())
                .call()) {
            return git.getRepository();
        } catch (GitAPIException e) {
            log.error("[{}] Something happened when trying to create a new local repository, error: [{}].", "1120_10082026", e.toString());
            throw new VestalException("1120_10082026", "Something happened when trying to create a new local repository.", e);
        }
    }

    public void setNewRemote(String path, RemoteRepository remoteRepository) {
        log.info("[{}] Setting a new remote for the local repository.", "1121_10082026");
        Repository repository = getExistingLocalRepo(path);
        try (Git git = new Git(repository)) {
            List<RemoteConfig> remotes;
            try {
                remotes = git.remoteList().call();
            } catch (GitAPIException e) {
                log.error("[{}] Something happened when trying to retrieve the list of remote repositories, error: [{}].", "1123_10082026", e.toString());
                throw new VestalException("1123_10082026", "Something happened when trying to retrieve the list of remote repositories.", e);
            }

            for (RemoteConfig remote : remotes) {
                if (!remote.getName().equals(remoteRepository.name)) {
                    try {
                        git.remoteRemove()
                                .setRemoteName(remote.getName())
                                .call();
                    } catch (GitAPIException e) {
                        log.error("[{}] Something happened when trying to remove a remote repository, error: [{}].", "1124_10082026", e.toString());
                        throw new VestalException("1124_10082026", "Something happened when trying to remove a remote repository.", e);
                    }
                }
            }

            try {
                git.remoteSetUrl()
                        .setRemoteName(remoteRepository.name)
                        .setRemoteUri(new URIish(remoteRepository.url))
                        .call();
            } catch (GitAPIException | URISyntaxException e) {
                log.error("[{}] Something happened when trying to set the new remote repository, error: [{}].", "1125_10082026", e.toString());
                throw new VestalException("1125_10082026", "Something happened when trying to set the new remote repository.", e);
            }
        }
    }
}
