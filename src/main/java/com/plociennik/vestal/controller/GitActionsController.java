package com.plociennik.vestal.controller;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.git.pull.RepoPullChangesService;
import com.plociennik.vestal.git.push.RepoPushChangesService;
import com.plociennik.vestal.git.status.GitStatusService;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Slf4j
public class GitActionsController extends VBox implements Initializable {

    @FXML private Text localRepoPathText;
    @FXML private Text remoteRepoNameText;
    @FXML private Text statusLabelText;
    @FXML private Button checkStatusButton;
    @FXML private Button pushChangesButton;
    @FXML private Button pullChangesButton;
    @FXML private ProgressIndicator busyIndicator;

    private StringProperty localRepoPathProperty = new SimpleStringProperty(null);
    private StringProperty remoteRepoNameProperty = new SimpleStringProperty(null);

    private GitStatusService gitStatusService = new GitStatusService();
    private RepoPushChangesService repoPushChangesService = new RepoPushChangesService();
    private RepoPullChangesService repoPullChangesService = new RepoPullChangesService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabelText.setText("Ready to work.");
        setupCheckStatusButton();
        setupPushChangesButton();
        setupPullChangesButton();

        ReadOnlyProperty<AppConfig> currentConfig = AppConfigManager.getInstance().getCurrentConfigProperty();

        localRepoPathProperty.setValue(currentConfig.getValue().vestalRepository.localRepository.sourcePath);
        remoteRepoNameProperty.setValue(currentConfig.getValue().vestalRepository.remoteRepository.name);

        currentConfig.addListener((observable, oldConfig, newConfig) -> {
            log.info("[{}] Listener for [{}] has been added.", "1033_07102026", "GitActionsController");
            localRepoPathProperty.setValue(newConfig.vestalRepository.localRepository.sourcePath);
            remoteRepoNameProperty.setValue(newConfig.vestalRepository.remoteRepository.name);
        });

        localRepoPathText.textProperty().bind(localRepoPathProperty);
        remoteRepoNameText.textProperty().bind(remoteRepoNameProperty);
    }

    private void setupCheckStatusButton() {
        checkStatusButton.setOnAction(e -> {
            startBusyIndicator();

            Task<Boolean> isCleanTask = new Task<>() {
                @Override
                protected Boolean call() {
                    return gitStatusService.isClean();
                }
            };

            isCleanTask.setOnSucceeded(evt -> {

                stopBusyIndicator();

                try {
                    Boolean isClean = isCleanTask.get();
                    if (isClean) {
                        log.info("[{}] There are no changes to be pushed.", "1258_31082026");
                        statusLabelText.setText("There are no changes to be pushed.");
                    } else {
                        log.info("[{}] There are changes to be pushed.", "1257_31082026");
                        statusLabelText.setText("There are changes to be pushed.");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new VestalException("1015_01102026", "Something happened while trying to check status.", ex);
                }
            });

            isCleanTask.setOnFailed(evt -> {
                        stopBusyIndicator();
                        statusLabelText.setText("Something happened while trying to check status, check logs.");
                        throw new VestalException(
                                "0913_01102026",
                                "Something happened while trying to check status.",
                                (Exception) isCleanTask.getException());
                    }
            );

            new Thread(isCleanTask).start();
        });
    }

    private void setupPushChangesButton() {
        pushChangesButton.setOnAction(e -> {
            startBusyIndicator();

            Task<Void> pushTask = new Task<>() {
                @Override
                protected Void call() {
                    log.info("[{}] Checking if there are any changes that warrant a push.", "1130_01092026");
                    boolean clean = gitStatusService.isClean();
                    if (clean) {
                        log.info("[{}] There were no changes, cancelling the process.", "1232_10082026");
                        statusLabelText.setText("There are no changes to be pushed.");
                        return null;
                    }
                    log.info("[{}] There are changes, pushing current state to remote.", "1222_10082026");

                    repoPushChangesService.push();
                    return null;
                }
            };

            pushTask.setOnSucceeded(evt -> {
                stopBusyIndicator();
                statusLabelText.setText("Push completed.");
            });

            pushTask.setOnFailed(evt -> {
                stopBusyIndicator();
                statusLabelText.setText("Something happened while trying to check status, check logs.");
                throw new VestalException(
                        "0925_01102026",
                        "Something happened while trying to check status.",
                        (Exception) pushTask.getException());
            });

            new Thread(pushTask).start();
        });
    }

    private void setupPullChangesButton() {
        pullChangesButton.setOnAction(e -> {
            startBusyIndicator();

            Task<Void> pullTask = new Task<>() {
                @Override
                protected Void call() {
                    repoPullChangesService.pull();
                    return null;
                }
            };

            pullTask.setOnSucceeded(evt -> {
                stopBusyIndicator();
                statusLabelText.setText("Pull completed.");
            });

            pullTask.setOnFailed(evt -> {
                stopBusyIndicator();
                statusLabelText.setText("Something happened while trying to check status, check logs.");
                throw new VestalException(
                        "0926_01102026",
                        "Something happened while trying to check status.",
                        (Exception) pullTask.getException());
            });

            new Thread(pullTask).start();
        });
    }

    private void startBusyIndicator() {
        busyIndicator.setVisible(true);
        busyIndicator.setManaged(true);
        statusLabelText.setVisible(false);
    }

    private void stopBusyIndicator() {
        busyIndicator.setVisible(false);
        busyIndicator.setManaged(false);
        statusLabelText.setVisible(true);
    }
}
