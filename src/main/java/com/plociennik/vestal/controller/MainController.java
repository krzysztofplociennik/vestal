package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.MainDirectoryInitService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable  {

    @FXML private VBox setupActions;
    @FXML private VBox gitActions;
    @FXML private GithubLoginController githubLoginController;
    @FXML private SetupActionsController setupActionsController;
    private MainDirectoryInitService mainDirectoryInitService = new MainDirectoryInitService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // todo: init a vestal main directory for vestal repositories
        mainDirectoryInitService.init();

        setupActions.visibleProperty().bind(githubLoginController.isUserLoggedInProperty());
        gitActions.visibleProperty().bind(setupActionsController.getAreDirectoryRepositoryPresent());
    }
}
