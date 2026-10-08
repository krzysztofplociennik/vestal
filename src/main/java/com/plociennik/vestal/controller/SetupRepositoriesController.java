package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.config.RemoteRepository;
import com.plociennik.vestal.git.fetch.GitFetchRepository;
import com.plociennik.vestal.git.fetch.GithubRepositoryFetcher;
import com.plociennik.vestal.git.init.LocalRepoManager;
import com.plociennik.vestal.state.State;
import com.plociennik.vestal.state.StateService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

// todo: naming should be more like: SetupRepositoriesController

@Slf4j
public class SetupRepositoriesController extends VBox implements Initializable {

    @FXML private HBox setupRepositoriesSubArea;
    @FXML private VBox localRepoArea;
    @FXML private VBox remoteRepoArea;
    @FXML private Button addChangeLocalRepoButton;
    @FXML private Text localRepoTitleText;
    @FXML private Button addChangeRemoteRepoButton;
    @FXML private Text remoteRepoTitleText;

    private AppConfigManager configManager = AppConfigManager.getInstance();
    private StateService stateService = StateService.getInstance();
    private GithubRepositoryFetcher githubRepositoryFetcher = new GithubRepositoryFetcher();
    private LocalRepoManager localRepoManager = new LocalRepoManager();

    private BooleanProperty isLocalRepoAbsent = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("[{}] SetupActionsController is initialized.", "1029_07102026");
        localRepoArea.prefWidthProperty().bind(setupRepositoriesSubArea.widthProperty().divide(2));
        remoteRepoArea.prefWidthProperty().bind(setupRepositoriesSubArea.widthProperty().divide(2));

        addChangeLocalRepoButton.textProperty().bind(
                isLocalRepoAbsent.map(isAbsent -> isAbsent ? "add" : "change"));
        addChangeRemoteRepoButton.disableProperty().bind(isLocalRepoAbsent);

        isLocalRepoAbsent.bind(configManager.getCurrentConfigProperty()
                .map(p -> p.vestalRepository.localRepository.sourcePath)
                .map(String::isBlank));

        localRepoTitleText.textProperty().bind(configManager.getCurrentConfigProperty()
                .map(c -> c.vestalRepository.localRepository.sourcePath));

        remoteRepoTitleText.textProperty().bind(configManager.getCurrentConfigProperty()
                .map(c -> c.vestalRepository.remoteRepository.name));

        setupAddChangeLocalRepoButton();
        setupAddRemoteRepoButton();
    }

    private void setupAddChangeLocalRepoButton() {
        addChangeLocalRepoButton.setOnAction(e -> {
            addChangeLocalRepoButton.setDisable(true);
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose a directory");

            String currentPath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath;
            if (currentPath != null && !currentPath.isBlank()) {
                File currentDir = new File(currentPath);
                if (currentDir.isDirectory()) {
                    directoryChooser.setInitialDirectory(currentDir);
                }
            }

            File selectedDirectory = directoryChooser.showDialog(addChangeLocalRepoButton.getScene().getWindow());

            if (selectedDirectory != null) {
                String localRepoPath = selectedDirectory.getAbsolutePath();
                configManager.update(ac -> {
                    ac.vestalRepository.localRepository = new LocalRepository(localRepoPath);
                    ac.vestalRepository.remoteRepository.clear();
                });
                AppConfig currentConfig = configManager.getCurrentConfig();
                localRepoManager.setupLocalRepository(currentConfig.vestalRepository.localRepository);
                log.info("[{}] A new local repository path: [{}] has been set.", "1122_040826", localRepoPath);
            } else {
                log.info("[{}] Directory selection was cancelled by the user.", "1123_040826");
            }
            addChangeLocalRepoButton.setDisable(false);
        });
    }

    private void setupAddRemoteRepoButton() {
        addChangeRemoteRepoButton.setOnAction(e -> {
            log.info("[{}] Loading repositories...", "0820_050826");

            Task<List<GitFetchRepository>> fetchTask = new Task<>() {
                @Override
                protected List<GitFetchRepository> call() throws Exception {
                    return githubRepositoryFetcher.fetchAllRepositories();
                }
            };

            fetchTask.setOnSucceeded(evt -> {
                List<GitFetchRepository> repositories = fetchTask.getValue();

                if (repositories.isEmpty()) {
                    addChangeRemoteRepoButton.setDisable(false);
                    log.info("[{}] No repositories found.", "0823_050826");
                    return;
                }

                showRepositoryPickerDialog(repositories).ifPresentOrElse(
                        result -> {
                            // todo: maybe a good idea would be to have a check for not selecting a wrong repository
                            // todo 2: maybe automatic pull should happen?
                            String repositoryName = result.repository.name();
                            String repositoryUrl = result.repository.cloneUrl();
                            localRepoManager.setRemoteOrigin(new RemoteRepository(repositoryName, repositoryUrl));
                            configManager.update(ac -> {
                                ac.vestalRepository.remoteRepository.name = repositoryName;
                                ac.vestalRepository.remoteRepository.url = repositoryUrl;
                            });
                            stateService.setState(State.ACTIONS);
                            log.info("[{}] Repository [{}] has been saved.", "1602_040826", repositoryName);
                        },
                        () -> log.info("[{}] Repository selection was cancelled.", "1603_040826")
                );
            });

            fetchTask.setOnFailed(evt -> {
                addChangeRemoteRepoButton.setDisable(false);
                log.warn("[{}] Failed to fetch repositories, error: {}", "1604_040826", fetchTask.getException().getMessage());
            });

            new Thread(fetchTask, "github-fetch-repos").start();
        });
    }

    private Optional<RepositorySelection> showRepositoryPickerDialog(List<GitFetchRepository> repositories) {
        Dialog<RepositorySelection> dialog = new Dialog<>();
        dialog.setTitle("Choose a repository");
        dialog.getDialogPane().setPrefSize(600, 700);
        dialog.getDialogPane().setMinSize(400, 400);
        dialog.setResizable(true);

        ListView<GitFetchRepository> listView = new ListView<>(FXCollections.observableArrayList(repositories));
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GitFetchRepository repo, boolean empty) {
                super.updateItem(repo, empty);
                setText(empty || repo == null ? null : repo.name());
            }
        });

        VBox.setVgrow(listView, Priority.ALWAYS);
        VBox content = new VBox(10, listView);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ButtonBar buttonBar = (ButtonBar) dialog.getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder("L_E+U+FBXI_YNOCAH_R");

        dialog.getDialogPane().lookupButton(selectButtonType)
                .disableProperty()
                .bind(listView.getSelectionModel().selectedItemProperty().isNull());

        dialog.setResultConverter(buttonType ->
                buttonType == selectButtonType
                        ? new RepositorySelection(listView.getSelectionModel().getSelectedItem())
                        : null);

        return dialog.showAndWait();
    }

    private record RepositorySelection(GitFetchRepository repository) {}
}
