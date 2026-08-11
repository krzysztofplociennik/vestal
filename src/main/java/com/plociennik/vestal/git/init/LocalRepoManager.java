package com.plociennik.vestal.git.init;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.config.RemoteRepository;
import com.plociennik.vestal.git.util.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
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
            repository = GitUtils.getExistingLocalRepo(directory.path);
            log.info("[{}] Local repo already exists, identifier [{}].", "1115_10082026", repository.getIdentifier());
        } else {
            log.info("[{}] Local repo does not exist for the directory [{}], I am creating it now.", "1112_10082026", directory.path);
            initRepo(pathToLocalRepo);
        }
    }

    private void initRepo(Path directory) {
        try (Git git = Git.init()
                .setDirectory(directory.toFile())
                .call()) {
            log.info("[{}] Local repo of identifier [{}] has been created.", "1116_10082026", git.getRepository().getIdentifier());
        } catch (GitAPIException e) {
            log.error("[{}] Something happened when trying to create a new local repository, error: [{}].", "1120_10082026", e.toString());
            throw new VestalException("1120_10082026", "Something happened when trying to create a new local repository.", e);
        }
    }

    public void setRemoteOrigin(String path, RemoteRepository remoteRepository) {
        log.info("[{}] Setting a new remote for the local repository.", "1121_10082026");
        final String ORIGIN = "origin";
        Repository repository = GitUtils.getExistingLocalRepo(path);
        try (Git git = new Git(repository)) {
            List<RemoteConfig> remotes;
            try {
                remotes = git.remoteList().call();
            } catch (GitAPIException e) {
                log.error("[{}] Something happened when trying to retrieve the list of remote repositories, error: [{}].", "1123_10082026", e.toString());
                throw new VestalException("1123_10082026", "Something happened when trying to retrieve the list of remote repositories.", e);
            }

            for (RemoteConfig remote : remotes) {
                if (!remote.getName().equals(ORIGIN)) {
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

            boolean originExists = remotes.stream().anyMatch(r -> r.getName().equals(ORIGIN));

            try {
                if (originExists) {
                    git.remoteSetUrl()
                            .setRemoteName(ORIGIN)
                            .setRemoteUri(new URIish(remoteRepository.url))
                            .call();
                } else {
                    git.remoteAdd()
                            .setName(ORIGIN)
                            .setUri(new URIish(remoteRepository.url))
                            .call();
                }
            } catch (GitAPIException | URISyntaxException e) {
                log.error("[{}] Something happened when trying to set the new remote repository, error: [{}].", "1125_10082026", e.toString());
                throw new VestalException("1125_10082026", "Something happened when trying to set the new remote repository.", e);
            }
        }
    }
}
