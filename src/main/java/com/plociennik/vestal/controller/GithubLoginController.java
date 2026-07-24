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
import javafx.scene.layout.HBox;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class GithubLoginController extends HBox implements Initializable {

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


    private void setupLoginButtonAction() {
        loginButton.setOnAction(e -> {
            statusLabel.setText("Requesting device code...");

            copyCodeButton.setVisible(true);

            Task<GitHubDeviceFlow.PollResult> task = new Task<>() {
                @Override
                protected GitHubDeviceFlow.PollResult call() throws Exception {
                    var deviceFlow = new GitHubDeviceFlow();
                    var deviceCode = deviceFlow.requestDeviceCode();

                    userCode = deviceCode.userCode();
                    System.out.println(userCode);

                    Platform.runLater(() -> {
                        codeLabel.setText("Enter this code: " + deviceCode.userCode()
                                + "\nOpen: " + deviceCode.verificationUri());
                        try {
                            BrowserLauncher.openUrl(deviceCode.verificationUri());
                        } catch (Exception ex) {
                            statusLabel.setText("Couldn't open browser automatically — use the URL above.");
                        }
                    });

                    return deviceFlow.pollForToken(deviceCode);
                }
            };

            task.setOnSucceeded(e2 -> {
                loginButton.setDisable(false);
                var result = task.getValue();
                if (result.success()) {
                    tokenStorage.save(result.accessToken());
                    statusLabel.setText("Logged in!");
                } else {
                    statusLabel.setText(result.error());
                }
            });

            task.setOnFailed(e2 -> {
                task.getException().printStackTrace();
                loginButton.setDisable(false);
                statusLabel.setText("Login failed: " + task.getException().getMessage());
            });

            new Thread(task, "github-device-login").start();
        });
    }

    @FXML
    protected void onLoginButtonClick() {
        loginButton.setVisible(false);
        logoutButton.setVisible(true);
    }

    @FXML
    protected void onLogoutButtonClick() {
        loginButton.setVisible(true);
        logoutButton.setVisible(false);
    }


    @FXML
    private void onCopyCodeClick() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(userCode.trim());
        clipboard.setContents(selection, null);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean isValid = tokenValidator.existsValidToken();
        if (isValid) {
            loginButton.setVisible(false);
            logoutButton.setVisible(true);
        } else {
            loginButton.setVisible(true);
            logoutButton.setVisible(false);
        }
        setupLoginButtonAction();
    }
}
