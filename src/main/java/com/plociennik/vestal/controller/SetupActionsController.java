package com.plociennik.vestal.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class SetupActionsController extends VBox implements Initializable {

    @FXML private Text statusLabelText;

    private boolean directorySaved = false;
    private boolean repositorySaved = false;

    private String directoryPath = "";
    private String repositoryName = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!directorySaved || !repositorySaved) {
            statusLabelText.setText("Directory and repository has not been set yet.");
            log.info("[{}] The directory and the repository has not been set yet.", "1311_310726");
        } else {
            statusLabelText.setText("Directory and repository has not been set yet.");
            log.info("[{}] The directory is set to: [{}] and the repository has been set to [{}]", "1313_310726", directoryPath, repositoryName);
        }
    }

    private void setupAddChangeDirectoryButton() {

    }

    private void setupAddChangeRepositoryButton() {

    }
}
