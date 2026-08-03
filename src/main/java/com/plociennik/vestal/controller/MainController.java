package com.plociennik.vestal.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable  {

    // todo: this is some stupid inconsistency that probably needs to be fixed
    @FXML private VBox setupActionsVBox;
    @FXML private GithubLoginController githubLoginController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupActionsVBox.visibleProperty().bind(githubLoginController.isUserLoggedInProperty());
    }
}
