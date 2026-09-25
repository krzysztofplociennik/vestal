package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.MainDirectoryService;
import com.plociennik.vestal.git.status.GitStatusService;
import com.plociennik.vestal.state.StateService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable  {

    @FXML private VBox githubLogin;
    @FXML private VBox setupActions;
    @FXML private VBox gitActions;
    @FXML private GithubLoginController githubLoginController;
    @FXML private SetupActionsController setupActionsController;

    @FXML private HBox dummyTest;

    private MainDirectoryService mainDirectoryService = new MainDirectoryService();

    private StateService stateService = StateService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainDirectoryService.init();
        githubLogin.visibleProperty().bind(stateService.getLoginVisible());
        setupActions.visibleProperty().bind(stateService.getRepositoriesVisible());
        gitActions.visibleProperty().bind(stateService.getActionsVisible());

        log.info("[{}] App initialized, ready to work.", "1251_17092026");
    }
}
