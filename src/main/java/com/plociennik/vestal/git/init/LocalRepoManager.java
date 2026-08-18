package com.plociennik.vestal.git.init;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.config.RemoteRepository;
import com.plociennik.vestal.encryption.FileEncryptor;
import com.plociennik.vestal.encryption.KeyDerivation;
import com.plociennik.vestal.git.util.GitUtils;
import com.plociennik.vestal.security.CredentialType;
import com.plociennik.vestal.security.CredentialsStorage;
import com.plociennik.vestal.security.KeyringCredentialsStorage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LocalRepoManager {

    // todo: structure maybe should be delegated, there are many functions here

    public void setupLocalRepository(LocalRepository localRepository) {
        log.info("[{}] I am checking if the local git repository is present.", "1416_07082026");
        Path pathToLocalRepo = Path.of(localRepository.encryptionPath);
        File gitDir = new File(pathToLocalRepo.toFile(), ".git");

        Repository repository;
        if (gitDir.exists()) {
            repository = GitUtils.getExistingLocalRepo();
            log.info("[{}] Local repo already exists, identifier [{}].", "1115_10082026", repository.getIdentifier());
        } else {
            log.info("[{}] Local repo does not exist, I am creating it now.", "1305_18082026");
            createEncryptionPath(localRepository);
            // todo: findTextFiles should be delegated, will be used at least 2 times
            // todo 2: if there are no files - maybe ignore code below
            List<Path> filesToEncrypt = findTxtFiles(Path.of(localRepository.sourcePath));
            FileEncryptor fileEncryptor = new FileEncryptor();
            Path destinationPath = Path.of(localRepository.encryptionPath);

            CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();
            AppConfig currentConfig = AppConfigManager.getInstance().getCurrentConfig();
            String secretAsString = credentialsStorage.get(CredentialType.ENCRYPTION_SECRET_KEY);
            byte[] encryptionSalt = currentConfig.getEncryptionSalt();
            SecretKey secretKeyContent;
            SecretKey secretKeyFilename;
            try {
                secretKeyContent = KeyDerivation.deriveKey(secretAsString.toCharArray(), encryptionSalt, KeyDerivation.PURPOSE_CONTENT);
                secretKeyFilename = KeyDerivation.deriveKey(secretAsString.toCharArray(), encryptionSalt, KeyDerivation.PURPOSE_FILENAME);
            } catch (GeneralSecurityException e) {
                throw new VestalException("1141_18082026", "Something happened when trying to retrieve secret keys.", e);
            }

            for (Path sourcePath : filesToEncrypt) {
                try {
                    FileEncryptor.EncryptResult encryptResult = fileEncryptor.encryptFile(sourcePath, destinationPath, secretKeyContent, secretKeyFilename);
                    Files.write(encryptResult.destFile(), encryptResult.output());
                } catch (IOException e) {
                    throw new VestalException("1142_18082026", "Something happened when trying to encrypt a file.", e);
                }
            }
            initRepo(pathToLocalRepo);
        }
    }

    private void createEncryptionPath(LocalRepository repository) {
        try {
            Files.createDirectories(Path.of(repository.encryptionPath));
        } catch (IOException e) {
            throw new VestalException("1837_13082026", "Could not create repository of [%s].".formatted(repository.encryptionPath), e);
        }
    }

    // todo: i think it does not exactly work
    private List<Path> findTxtFiles(Path workDir) {
        List<Path> txtFiles = new ArrayList<>();

        try {
            Files.walkFileTree(workDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName() != null && dir.getFileName().toString().equals(".git")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    log.info("Visiting: {}", file);
                    txtFiles.add(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new VestalException("1140_18082026", "Something happened while filtering txt files.", e);
        }

        return txtFiles;
    }

    private void initRepo(Path directory) {
        try (Git git = Git.init()
                .setDirectory(directory.toFile())
                .call()) {
            log.info("[{}] A git repo of the identifier [{}] has been created.", "1116_10082026", git.getRepository().getIdentifier());
        } catch (GitAPIException e) {
            throw new VestalException("1120_10082026", "Something happened when trying to create a new local repository.", e);
        }
    }

    public void setRemoteOrigin(RemoteRepository remoteRepository) {
        log.info("[{}] Setting a new remote for the local repository.", "1121_10082026");
        final String ORIGIN = "origin";
        Repository repository = GitUtils.getExistingLocalRepo();
        try (Git git = new Git(repository)) {
            List<RemoteConfig> remotes;
            try {
                remotes = git.remoteList().call();
            } catch (GitAPIException e) {
                throw new VestalException("1123_10082026", "Something happened when trying to retrieve the list of remote repositories.", e);
            }

            for (RemoteConfig remote : remotes) {
                if (!remote.getName().equals(ORIGIN)) {
                    try {
                        git.remoteRemove()
                                .setRemoteName(remote.getName())
                                .call();
                    } catch (GitAPIException e) {
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
                throw new VestalException("1125_10082026", "Something happened when trying to set the new remote repository.", e);
            }
        }
    }
}
