package com.plociennik.vestal.controller;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabelText.setText("Initial state.");
        setupCheckStatusButton();
        setupPushChangesButton();
        setupPullChangesButton();
    }

    private void setupCheckStatusButton() {

    }

    private void checkStatus() {

        // simplest implementation so far (i dont think i need more, like merge changes or check status)
        // if local is empty - one pull from remote
        // if local is not empty - check dates only, pull if local is half empty or older than remote
        // pull only newer files
        // push - pretty much will always be newer than remote
        // remote is usually a backup and should stay like that
        // if both are empty - disable pull
    }

    private void setupPushChangesButton() {

    }

    private void setupPullChangesButton() {

    }
}
