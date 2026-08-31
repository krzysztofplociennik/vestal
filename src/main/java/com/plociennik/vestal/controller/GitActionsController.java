package com.plociennik.vestal.controller;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.encryption.EncryptionService;
import com.plociennik.vestal.encryption.FileEncryptor;
import com.plociennik.vestal.git.push.RepoPushChangesService;
import com.plociennik.vestal.git.status.GitStatusService;
import com.plociennik.vestal.git.util.FilesUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class GitActionsController extends VBox implements Initializable {

    @FXML private Text statusLabelText;
    @FXML private Button checkStatusButton;
    @FXML private Button pushChangesButton;
    @FXML private Button pullChangesButton;

    private RepoPushChangesService repoPushChangesService = new RepoPushChangesService();
    private EncryptionService encryptionService = new EncryptionService();
    private GitStatusService gitStatusService = new GitStatusService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabelText.setText("Initial state.");
        setupCheckStatusButton();
        setupPushChangesButton();
        setupPullChangesButton();
    }

    private void setupCheckStatusButton() {
        checkStatusButton.setOnAction(e -> {
            boolean statusChanged = gitStatusService.isStatusChanged();
            if (statusChanged) {
                log.info("[{}] There are changes to be pushed.", "1257_31082026");
                statusLabelText.setText("There are changes to be pushed.");
            } else {
                log.info("[{}] There are no changes to be pushed.", "1258_31082026");
                statusLabelText.setText("There are no changes to be pushed.");
            }
        });
    }

    private void setupPushChangesButton() {
        pushChangesButton.setOnAction(e -> {
            // todo: these methods maybe should be in a different class
            AppConfig currentConfig = AppConfigManager.getInstance().getCurrentConfig();
            // clear existing files from the local encrypted repository
            clearExistingFiles(
                    Path.of(currentConfig.vestalRepository.localRepository.encryptionPath)
            );
            // copy and encrypt current state
            encryptAndPaste(
                    Path.of(currentConfig.vestalRepository.localRepository.sourcePath),
                    Path.of(currentConfig.vestalRepository.localRepository.encryptionPath)
            );
            // push updated files
            repoPushChangesService.push();
            gitStatusService.updateManifest();
        });
    }

    private void clearExistingFiles(Path path) {
        log.info("[{}] Deleting existing files to make space for new files.", "1336_19082026");
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName() != null && dir.getFileName().toString().equals(".git")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    log.info("Deleting: {}", file);
                    FilesUtils.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new VestalException("1148_19082026", "Something happened while trying to delete files.", e);
        }
        log.info("[{}] The files have been deleted.", "1337_19082026");
    }

    private void encryptAndPaste(Path source, Path destination) {
        log.info("[{}] I am trying to move encrypted files into destination.", "1325_19082026");
        List<FileEncryptor.EncryptResult> paths = encryptionService.encryptPath(source, destination);
        for (FileEncryptor.EncryptResult path : paths) {
            try {
                Files.write(path.destinationFile(), path.output());
            } catch (IOException e) {
                throw new VestalException("1324_19082026", "Something happened when trying to move encrypted files into destination.", e);
            }
        }
        log.info("[{}] Encrypted files have been moved into destination.", "1332_19082026");
    }

    private void setupPullChangesButton() {
        pullChangesButton.setOnAction(e -> {
            // todo: to implement
            gitStatusService.updateManifest();
        });
    }
}
