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
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable  {

    @FXML private VBox landingView;
    @FXML private VBox githubLoginView;
    @FXML private VBox encryptionKeyView;
    @FXML private VBox setupRepositoriesView;
    @FXML private VBox gitActionsView;

    @FXML private Button changeRepositoriesButton;
    @FXML private Button logoutButton;

    private MainDirectoryService mainDirectoryService = new MainDirectoryService();
    private StateService stateService = StateService.getInstance();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainDirectoryService.init();
        githubLoginView.visibleProperty().bind(stateService.getLoginVisible());
        setupRepositoriesView.visibleProperty().bind(stateService.getRepositoriesVisible());
        gitActionsView.visibleProperty().bind(stateService.getActionsVisible());
        landingView.visibleProperty().bind(stateService.getLandingVisible());
        encryptionKeyView.visibleProperty().bind(stateService.getEncryptionKeyVisible());

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
