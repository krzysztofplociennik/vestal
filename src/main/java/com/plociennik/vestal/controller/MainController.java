package com.plociennik.vestal.controller;

import com.plociennik.vestal.config.AppConfig;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.MainDirectoryService;
import com.plociennik.vestal.encryption.SaltGenerator;
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
        mainDirectoryService.init();
        generateAndSaveSalt();
        gitStatusService.init();
        setupActions.visibleProperty().bind(githubLoginController.isUserLoggedInProperty());
        gitActions.visibleProperty().bind(setupActionsController.getAreDirectoryRepositoryPresent());
    }

    private void generateAndSaveSalt() {
        AppConfig currentConfig = AppConfigManager.getInstance().getCurrentConfig();
        if (currentConfig.encryptionSalt == null) {
            currentConfig.encryptionSalt = SaltGenerator.generateSalt();
            AppConfigManager.getInstance().saveConfig(currentConfig);
        }
    }
}
