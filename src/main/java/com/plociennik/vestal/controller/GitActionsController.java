package com.plociennik.vestal.controller;

import com.plociennik.vestal.git.pull.RepoPullChangesService;
import com.plociennik.vestal.git.push.RepoPushChangesService;
import com.plociennik.vestal.git.status.GitStatusService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class GitActionsController extends VBox implements Initializable {

    @FXML private Text statusLabelText;
    @FXML private Button checkStatusButton;
    @FXML private Button pushChangesButton;
    @FXML private Button pullChangesButton;

    private RepoPushChangesService repoPushChangesService = new RepoPushChangesService();
    private GitStatusService gitStatusService = new GitStatusService();
    private RepoPullChangesService repoPullChangesService = new RepoPullChangesService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabelText.setText("Initial state.");
        setupCheckStatusButton();
        setupPushChangesButton();
        setupPullChangesButton();
    }

    private void setupCheckStatusButton() {
        checkStatusButton.setOnAction(e -> {
            boolean isClean = gitStatusService.isClean();
            if (isClean) {
                log.info("[{}] There are no changes to be pushed.", "1258_31082026");
                statusLabelText.setText("There are no changes to be pushed.");
            } else {
                log.info("[{}] There are changes to be pushed.", "1257_31082026");
                statusLabelText.setText("There are changes to be pushed.");
            }
        });
    }

    private void setupPushChangesButton() {
        pushChangesButton.setOnAction(e -> {
            repoPushChangesService.push();
        });
    }

    private void setupPullChangesButton() {
        pullChangesButton.setOnAction(e -> {
            repoPullChangesService.pull();
        });
    }
}
