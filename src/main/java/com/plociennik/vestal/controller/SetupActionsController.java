package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.LocalRepository;
import com.plociennik.vestal.config.RemoteRepository;
import com.plociennik.vestal.git.fetch.GitFetchRepository;
import com.plociennik.vestal.git.fetch.GithubRepositoryFetcher;
import com.plociennik.vestal.git.init.LocalRepoManager;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class SetupActionsController extends VBox implements Initializable {

    @FXML private HBox setupActionsSubArea;
    @FXML private VBox directoryArea;
    @FXML private VBox repositoryArea;
    @FXML private Text statusLabelText;
    @FXML private Button addChangeDirectoryButton;
    @FXML private Text directoryTitleText;
    @FXML private Button addChangeRepositoryButton;
    @FXML private Text repositoryTitleText;

    private AppConfigManager configManager = AppConfigManager.getInstance();
    private final GithubRepositoryFetcher githubRepositoryFetcher = new GithubRepositoryFetcher();
    private final LocalRepoManager localRepoManager = new LocalRepoManager();

    private String directoryPath = null;
    private String repositoryName = null;
    private String repositoryUrl = null;

    @Getter private BooleanProperty areDirectoryRepositoryPresent = new SimpleBooleanProperty(false);
    @Getter private BooleanProperty isDirectoryAbsent = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        directoryArea.prefWidthProperty().bind(setupActionsSubArea.widthProperty().divide(2));
        repositoryArea.prefWidthProperty().bind(setupActionsSubArea.widthProperty().divide(2));

        directoryPath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath;
        repositoryName = configManager.getCurrentConfig().vestalRepository.remoteRepository.name;
        addChangeRepositoryButton.disableProperty().bind(isDirectoryAbsent);

        boolean isDirectoryPathBlank = StringUtils.isBlank(directoryPath);
        boolean isRepositoryNameBlank = StringUtils.isBlank(repositoryName);

        isDirectoryAbsent.set(isDirectoryPathBlank);

        directoryTitleText.setText(isDirectoryPathBlank ? "empty" : directoryPath);
        repositoryTitleText.setText(isRepositoryNameBlank ? "empty" : repositoryName);

        addChangeDirectoryButton.setText(isDirectoryPathBlank ? "add" : "change");
        addChangeRepositoryButton.setText(isRepositoryNameBlank ? "add" : "change");

        if (isDirectoryPathBlank || isRepositoryNameBlank) {
            statusLabelText.setText("Both directory and repository need to be set.");
            log.info("[{}] Both directory and repository needs to be set.", "1311_310726");
        } else {
            areDirectoryRepositoryPresent.set(true);
            statusLabelText.setText("All set, time to work.");
            log.info("[{}] The directory is set to: [{}] and the repository has been set to [{}]", "1313_310726", directoryPath, repositoryName);
        }

        setupAddChangeDirectoryButton();
        setupAddChangeRepositoryButton();
    }

    private void setupAddChangeDirectoryButton() {
        addChangeDirectoryButton.setOnAction(e -> {
            addChangeDirectoryButton.setDisable(true);
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose a directory");

            String currentPath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath;
            if (currentPath != null && !currentPath.isBlank()) {
                File currentDir = new File(currentPath);
                if (currentDir.isDirectory()) {
                    directoryChooser.setInitialDirectory(currentDir);
                }
            }

            File selectedDirectory = directoryChooser.showDialog(addChangeDirectoryButton.getScene().getWindow());

            if (selectedDirectory != null) {
                directoryPath = selectedDirectory.getAbsolutePath();
                AppConfig currentConfig = configManager.getCurrentConfig();
                currentConfig.vestalRepository.localRepository = new LocalRepository(directoryPath);
                configManager.saveConfig(currentConfig);
                localRepoManager.setupLocalRepository(currentConfig.vestalRepository.localRepository);
                directoryTitleText.setText(directoryPath);
                isDirectoryAbsent.set(false);
                handleIfDirectoryAndRepositoryBothPresent();
                log.info("[{}] A new directory path: [{}] has been set.", "1122_040826", directoryPath);
            } else {
                log.info("[{}] Directory selection was cancelled by the user.", "1123_040826");
            }
            addChangeDirectoryButton.setDisable(false);
        });
    }

    private void setupAddChangeRepositoryButton() {
        addChangeRepositoryButton.setOnAction(e -> {
            repositoryTitleText.setText("Loading repositories...");
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
                    addChangeRepositoryButton.setDisable(false);
                    repositoryTitleText.setText("No repositories found.");
                    log.info("[{}] No repositories found.", "0823_050826");
                    return;
                }

                showRepositoryPickerDialog(repositories).ifPresentOrElse(
                        result -> {
                            // todo: maybe a good idea would be to have a check for not selecting a wrong repository
                            // todo 2: maybe automatic pull should happen?
                            repositoryName = result.repository.name();
                            repositoryUrl = result.repository.cloneUrl();
                            AppConfig currentConfig = configManager.getCurrentConfig();
                            currentConfig.vestalRepository.remoteRepository.name = repositoryName;
                            currentConfig.vestalRepository.remoteRepository.url = repositoryUrl;
                            configManager.saveConfig(currentConfig);
                            localRepoManager.setRemoteOrigin(new RemoteRepository(repositoryName, repositoryUrl));
                            repositoryTitleText.setText(repositoryName);
                            handleIfDirectoryAndRepositoryBothPresent();
                            log.info("[{}] Repository [{}] has been saved.", "1602_040826", repositoryName);
                        },
                        () -> {
                            repositoryTitleText.setText(configManager.getCurrentConfig().vestalRepository.remoteRepository.name);
                            log.info("[{}] Repository selection was cancelled.", "1603_040826");
                        }
                );
            });

            fetchTask.setOnFailed(evt -> {
                addChangeRepositoryButton.setDisable(false);
                repositoryTitleText.setText("Failed to load repositories.");
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
        CheckBox checkBox = new CheckBox("Encrypt notes in this repository");

        VBox.setVgrow(listView, Priority.ALWAYS);
        VBox content = new VBox(10, listView, checkBox);
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
                        ? new RepositorySelection(listView.getSelectionModel().getSelectedItem(), checkBox.isSelected())
                        : null);

        return dialog.showAndWait();
    }

    private void handleIfDirectoryAndRepositoryBothPresent() {
        if (directoryPath != null && repositoryName != null) {
            areDirectoryRepositoryPresent.set(true);
        }
    }

    private record RepositorySelection(GitFetchRepository repository, boolean checkboxValue) {}
}
