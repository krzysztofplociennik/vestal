package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.AppConfigManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class SetupActionsController extends VBox implements Initializable {

    @FXML private Text statusLabelText;
    @FXML private Button addChangeDirectoryButton;
    @FXML private Text directoryTitleText;
    @FXML private Button addChangeRepositoryButton;
    @FXML private Text repositoryTitleText;

    private String directoryPath = "";
    private String repositoryName = "";

    private AppConfigManager configManager = new AppConfigManager();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        directoryPath = configManager.getCurrentConfig().directory.path;
        repositoryName = configManager.getCurrentConfig().repository.name;
        boolean isDirectoryPathEmpty = directoryPath == null;
        boolean isRepositoryNameEmpty = repositoryName == null;
        directoryTitleText.setText(isDirectoryPathEmpty ? "empty" : directoryPath);
        repositoryTitleText.setText(isRepositoryNameEmpty ? "empty" : repositoryName);
        if (isDirectoryPathEmpty || isRepositoryNameEmpty) {
            statusLabelText.setText("Both directory and repository needs to be set.");
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
            configManager.saveDirectoryPath("example path");
            directoryTitleText.setText(configManager.getCurrentConfig().directory.path);
            log.info("[{}] A new directory path: [{}] has been set.", "1544_020826", "EXAMPLE");
        });
    }

    private void setupAddChangeRepositoryButton() {

    }
}
