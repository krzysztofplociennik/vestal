package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.MainDirectoryService;
import com.plociennik.vestal.git.status.GitStatusService;
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
    private MainDirectoryService mainDirectoryService = new MainDirectoryService();
    private GitStatusService gitStatusService = new GitStatusService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // todo: need to think about, maybe state implementation9
        mainDirectoryService.init();
        setupActions.visibleProperty().bind(githubLoginController.isUserLoggedInProperty());
        gitActions.visibleProperty().bind(setupActionsController.getAreDirectoryRepositoryPresent());
        log.info("[{}] App initialized, ready to work.", "1251_17092026");
    }
}
