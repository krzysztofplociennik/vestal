package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.MainDirectoryService;
import com.plociennik.vestal.security.CredentialsStorage;
import com.plociennik.vestal.security.KeyringCredentialsStorage;
import com.plociennik.vestal.state.State;
import com.plociennik.vestal.state.StateService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable  {

    @FXML private Button changeRepositoriesButton;
    @FXML private HBox logoutButtonArea;
    @FXML private VBox landingPage;

    // todo: names should point that this is a page/view
    @FXML private VBox githubLogin;
    @FXML private VBox encryptionKey;
    @FXML private VBox setupActions;
    @FXML private VBox gitActions;

    @FXML private Button logoutButton;

    // todo: maybe unneeded
    @FXML private GithubLoginController githubLoginController;
    @FXML private SetupActionsController setupActionsController;

    private MainDirectoryService mainDirectoryService = new MainDirectoryService();
    private StateService stateService = StateService.getInstance();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainDirectoryService.init();
        githubLogin.visibleProperty().bind(stateService.getLoginVisible());
        setupActions.visibleProperty().bind(stateService.getRepositoriesVisible());
        gitActions.visibleProperty().bind(stateService.getActionsVisible());
        landingPage.visibleProperty().bind(stateService.getLandingVisible());
        encryptionKey.visibleProperty().bind(stateService.getEncryptionKeyVisible());
        logoutButton.visibleProperty().bind(stateService.getIsLoggedIn());
        changeRepositoriesButton.visibleProperty().bind(stateService.getActionsVisible());

        setupLogoutButton();
        setupChangeReposButton();
        applyState();
        log.info("[{}] App initialized, ready to work.", "1251_17092026");
    }

    private void applyState() {
        AppConfigManager manager = AppConfigManager.getInstance();
        State currentState = manager.getCurrentConfig().vestalRepository.state;
        stateService.setState(currentState);
    }

    private void setupLogoutButton() {
        logoutButton.setOnAction(e -> {
            credentialsStorage.clear();
            stateService.setState(State.LOGGED_OUT);
        });
    }

    private void setupChangeReposButton() {
        changeRepositoriesButton.setOnAction(e -> {
            AppConfigManager manager = AppConfigManager.getInstance();
            manager.update(c -> c.vestalRepository.clear());
            stateService.setState(State.REPOS);
        });
    }
}
