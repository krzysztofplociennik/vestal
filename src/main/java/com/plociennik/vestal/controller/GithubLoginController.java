package com.plociennik.vestal.controller;

import com.plociennik.vestal.git.BrowserLauncher;
import com.plociennik.vestal.git.GitHubDeviceFlow;
import com.plociennik.vestal.git.TokenStorage;
import com.plociennik.vestal.git.TokenValidator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class GithubLoginController extends VBox implements Initializable {

    @FXML
    private Button loginButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button copyCodeButton;

    @FXML
    private Label codeLabel;

    @FXML
    private Label statusLabel;

    private TokenStorage tokenStorage = new TokenStorage();
    private TokenValidator tokenValidator = new TokenValidator();
    private String userCode = "";
    private String login = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TokenValidator.AuthResult authResult = tokenValidator.validate();
        if (authResult.success()) {
            log.info("Current token is valid for login: [{}]", authResult.login());
            login = authResult.login();
            loginButton.setVisible(false);
            logoutButton.setVisible(true);
        } else {
            log.warn("Current token is not valid for login: [{}], reason: {}", authResult.login(), authResult.errorMessage());
            loginButton.setVisible(true);
            logoutButton.setVisible(false);
        }
        setupLoginButtonAction();
        setupLogoutButtonAction();
        setupCopyCodeButtonAction();
    }

    private void setupLoginButtonAction() {
        loginButton.setOnAction(e -> {
            statusLabel.setText("Requesting device code...");
            loginButton.setVisible(false);
            copyCodeButton.setVisible(true);

            Task<GitHubDeviceFlow.PollResult> task = new Task<>() {
                @Override
                protected GitHubDeviceFlow.PollResult call() throws Exception {
                    var deviceFlow = new GitHubDeviceFlow();
                    var deviceCode = deviceFlow.requestDeviceCode();
                    userCode = deviceCode.userCode();

                    Platform.runLater(() -> {
                        codeLabel.setText("Enter this code: " + deviceCode.userCode()
                                + "\nOpen: " + deviceCode.verificationUri());
                        try {
                            BrowserLauncher.openUrl(deviceCode.verificationUri());
                        } catch (Exception ex) {
                            statusLabel.setText("Couldn't open browser automatically — use the URL above.");
                            log.warn("Couldn't open browser automatically, error: {}", ex.getMessage());
                        }
                    });

                    return deviceFlow.pollForToken(deviceCode);
                }
            };

            task.setOnSucceeded(e2 -> {
                loginButton.setVisible(false);
                var result = task.getValue();
                if (result.success()) {
                    tokenStorage.save(result.accessToken());
                    loginButton.setVisible(false);
                    codeLabel.setVisible(false);
                    copyCodeButton.setVisible(false);
                    logoutButton.setVisible(true);
                    statusLabel.setText("Logged in as [%s]".formatted(login));
                    log.info("Successfully logged in as [{}].", login);
                } else {
                    statusLabel.setText(result.error());
                    log.warn("Login unsuccessful, error: {}", task.getException().getMessage());

                }
            });

            task.setOnFailed(e2 -> {
                loginButton.setVisible(true);
                log.warn("Login failed.");
                statusLabel.setText("Login failed, error: %s".formatted(task.getException().getMessage()));
            });

            new Thread(task, "github-device-login").start();
        });
    }

    private void setupLogoutButtonAction() {
        logoutButton.setOnAction(e -> {
            log.info("Logged out.");
            statusLabel.setText("Logged out.");
            loginButton.setVisible(true);
            logoutButton.setVisible(false);
            tokenStorage.clear();
        });
    }

    private void setupCopyCodeButtonAction() {
        copyCodeButton.setOnAction(e -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection selection = new StringSelection(userCode.trim());
            clipboard.setContents(selection, null);
            log.info("User code [{}] has been copied.", userCode);
        });
    }
}
