package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.AppConfigManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
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

    // todo: potentially not really needed
    private String directoryPath = "";
    private String repositoryName = "";

    private AppConfigManager configManager = new AppConfigManager();

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
            statusLabelText.setText("Directory: [%s] | Repository: [%s]".formatted(directoryPath, repositoryName));
            log.info("[{}] The directory is set to: [{}] and the repository has been set to [{}]", "1313_310726", directoryPath, repositoryName);
        }

        setupAddChangeDirectoryButton();
        setupAddChangeRepositoryButton();
    }

    private void setupAddChangeDirectoryButton() {
        addChangeDirectoryButton.setOnAction(e -> {
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
                log.info("[{}] A new directory path: [{}] has been set.", "1122_040826", selectedPath);
            } else {
                log.info("[{}] Directory selection was cancelled by the user.", "1123_040826");
            }
        });
    }

    private void setupAddChangeRepositoryButton() {

    }
}
