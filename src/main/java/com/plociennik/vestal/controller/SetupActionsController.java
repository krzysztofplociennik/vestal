package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.git.fetch.GitHubRepository;
import com.plociennik.vestal.git.fetch.GithubRepositoryFetcher;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

    private AppConfigManager configManager = new AppConfigManager();
    private final GithubRepositoryFetcher githubRepositoryFetcher = new GithubRepositoryFetcher();

    private String directoryPath = null;
    private String repositoryName = null;
    @Getter private BooleanProperty isDirectoryRepositorySetupProperty = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        directoryArea.prefWidthProperty().bind(setupActionsSubArea.widthProperty().divide(2));
        repositoryArea.prefWidthProperty().bind(setupActionsSubArea.widthProperty().divide(2));

        directoryPath = configManager.getCurrentConfig().directory.path;
        repositoryName = configManager.getCurrentConfig().repository.name;

        boolean isDirectoryPathEmpty = directoryPath == null;
        boolean isRepositoryNameEmpty = repositoryName == null;

        directoryTitleText.setText(isDirectoryPathEmpty ? "empty" : directoryPath);
        repositoryTitleText.setText(isRepositoryNameEmpty ? "empty" : repositoryName);

        addChangeDirectoryButton.setText(isDirectoryPathEmpty ? "add" : "change");
        addChangeRepositoryButton.setText(isRepositoryNameEmpty ? "add" : "change");

        if (isDirectoryPathEmpty || isRepositoryNameEmpty) {
            statusLabelText.setText("Both directory and repository need to be set.");
            log.info("[{}] Both directory and repository needs to be set.", "1311_310726");
        } else {
            isDirectoryRepositorySetupProperty.set(true);
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

            String currentPath = configManager.getCurrentConfig().directory.path;
            if (currentPath != null && !currentPath.isBlank()) {
                File currentDir = new File(currentPath);
                if (currentDir.isDirectory()) {
                    directoryChooser.setInitialDirectory(currentDir);
                }
            }

            File selectedDirectory = directoryChooser.showDialog(addChangeDirectoryButton.getScene().getWindow());

            if (selectedDirectory != null) {
                String selectedPath = selectedDirectory.getAbsolutePath();
                configManager.saveDirectoryPath(selectedPath);
                directoryTitleText.setText(selectedPath);
                directoryPath = selectedPath;
                setDirectoryRepositoryPropertyTrueIfBothPresent();
                log.info("[{}] A new directory path: [{}] has been set.", "1122_040826", selectedPath);
            } else {
                log.info("[{}] Directory selection was cancelled by the user.", "1123_040826");
            }
            addChangeDirectoryButton.setDisable(false);
        });
    }

    private void setupAddChangeRepositoryButton() {
        addChangeRepositoryButton.setOnAction(e -> {
            addChangeRepositoryButton.setDisable(true);
            repositoryTitleText.setText("Loading repositories...");
            log.info("[{}] Loading repositories...", "0820_050826");

            Task<List<GitHubRepository>> fetchTask = new Task<>() {
                @Override
                protected List<GitHubRepository> call() throws Exception {
                    return githubRepositoryFetcher.fetchAllRepositories();
                }
            };

            fetchTask.setOnSucceeded(evt -> {
                List<GitHubRepository> repositories = fetchTask.getValue();

                if (repositories.isEmpty()) {
                    addChangeRepositoryButton.setDisable(false);
                    repositoryTitleText.setText("No repositories found.");
                    log.info("[{}] No repositories found.", "0823_050826");
                    return;
                }

                showRepositoryPickerDialog(repositories).ifPresentOrElse(
                        selectedRepo -> {
                            configManager.saveRepositoryName(selectedRepo.name(), selectedRepo.cloneUrl());
                            repositoryTitleText.setText(selectedRepo.name());
                            addChangeRepositoryButton.setDisable(false);
                            repositoryName = selectedRepo.name();
                            setDirectoryRepositoryPropertyTrueIfBothPresent();
                            log.info("[{}] Repository [{}] has been saved.", "1602_040826", selectedRepo.name());
                        },
                        () -> {
                            repositoryTitleText.setText(configManager.getCurrentConfig().repository.name);
                            addChangeRepositoryButton.setDisable(false);
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

    private Optional<GitHubRepository> showRepositoryPickerDialog(List<GitHubRepository> repositories) {
        Dialog<GitHubRepository> dialog = new Dialog<>();
        dialog.setTitle("Choose a repository");
        dialog.getDialogPane().setPrefSize(600, 700);
        dialog.getDialogPane().setMinSize(400, 400);
        dialog.setResizable(true);

        ListView<GitHubRepository> listView = new ListView<>(FXCollections.observableArrayList(repositories));
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GitHubRepository repo, boolean empty) {
                super.updateItem(repo, empty);
                setText(empty || repo == null ? null : repo.name());
            }
        });

        dialog.getDialogPane().setContent(listView);
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ButtonBar buttonBar = (ButtonBar) dialog.getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder("L_E+U+FBXI_YNOCAH_R");

        dialog.getDialogPane().lookupButton(selectButtonType)
                .disableProperty()
                .bind(listView.getSelectionModel().selectedItemProperty().isNull());

        dialog.setResultConverter(buttonType ->
                buttonType == selectButtonType ? listView.getSelectionModel().getSelectedItem() : null);

        return dialog.showAndWait();
    }

    private void setDirectoryRepositoryPropertyTrueIfBothPresent() {
        if (!directoryPath.isEmpty() && !repositoryName.isEmpty()) {
            isDirectoryRepositorySetupProperty.set(true);
        }
    }
}
