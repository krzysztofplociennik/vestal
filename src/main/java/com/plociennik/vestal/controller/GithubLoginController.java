package com.plociennik.vestal.controller;

import com.plociennik.vestal.login.BrowserLauncher;
import com.plociennik.vestal.login.CredentialStorage;
import com.plociennik.vestal.login.CredentialType;
import com.plociennik.vestal.login.GitHubDeviceFlow;
import com.plociennik.vestal.login.TokenValidator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class GithubLoginController extends VBox implements Initializable {

    @FXML private Text statusText;
    @FXML private Button loginButton;
    @FXML private Button logoutButton;
    @FXML private VBox clientIdArea;
    @FXML private PasswordField clientIdField;
    @FXML private Button saveClientIdButton;
    @FXML private VBox userCodeArea;
    @FXML private Text codeInfo;
    @FXML private Text userCodeText;
    @FXML private Button copyCodeButton;
    @FXML private Text verificationLinkText;
    @FXML private Text verificationLinkInfo;
    @FXML private Button copyVerificationLinkButton;

    private CredentialStorage credentialStorage = new CredentialStorage();
    private TokenValidator tokenValidator = new TokenValidator();
    private String userCode = "";
    private String verificationLink = "";

    private boolean clientIdSaved;

    private BooleanProperty isUserLoggedIn = new SimpleBooleanProperty(false);

    public BooleanProperty isUserLoggedInProperty() {
        return isUserLoggedIn;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clientIdSaved = credentialStorage.get(CredentialType.GITHUB_CLIENT_ID).isPresent();
        clientIdArea.managedProperty().bind(clientIdArea.visibleProperty());
        userCodeArea.managedProperty().bind(userCodeArea.visibleProperty());
        loginButton.managedProperty().bind(loginButton.visibleProperty());
        logoutButton.managedProperty().bind(logoutButton.visibleProperty());

        TokenValidator.AuthResult authResult = tokenValidator.validate();
        if (authResult.success()) {
            log.info("Current token is valid for login: [{}]", authResult.login());
            statusText.setText("Logged in as [%s]".formatted(credentialStorage.get(CredentialType.GITHUB_LOGIN).get()));
            loginButton.setVisible(false);
            logoutButton.setVisible(true);
            isUserLoggedIn.set(true);
        } else {
            log.warn("Current token is not valid for login: [{}], reason: {}", authResult.login(), authResult.errorMessage());
            statusText.setText("Not logged in.");
            loginButton.setVisible(true);
            logoutButton.setVisible(false);
        }
        setupLoginButtonAction();
        setupLogoutButtonAction();
        setupCopyCodeButtonAction();
        setupSaveClientIdButton();
        setupVerificationLinkCopyButton();
    }

    private void setupLoginButtonAction() {
        loginButton.setOnAction(e -> {
            if (clientIdSaved) {
                statusText.setText("Requesting device code...");
                loginButton.setDisable(true);
                userCodeArea.setVisible(true);

                Task<GitHubDeviceFlow.PollResult> task = new Task<>() {
                    @Override
                    protected GitHubDeviceFlow.PollResult call() throws Exception {
                        var deviceFlow = new GitHubDeviceFlow();
                        var deviceCode = deviceFlow.requestDeviceCode();
                        userCode = deviceCode.userCode();
                        verificationLink = deviceCode.verificationUri();

                        Platform.runLater(() -> {
                            codeInfo.setText("Paste this user code for GH verification:");
                            userCodeText.setText(deviceCode.userCode());
                            statusText.setText("Waiting for GH response...");
                            verificationLinkInfo.setText("You can also visit the verification page manually by visiting the link below:");
                            verificationLinkText.setText(deviceCode.verificationUri());
                            try {
                                BrowserLauncher.openUrl(deviceCode.verificationUri());
                            } catch (Exception ex) {
                                statusText.setText("Couldn't open browser automatically — use the verification URL below.");
                                log.warn("Couldn't open browser automatically, error: {}", ex.getMessage());
                            }
                        });
                        return deviceFlow.pollForToken(deviceCode);
                    }
                };

                task.setOnSucceeded(e2 -> {
                    if (clientIdSaved) {
                        loginButton.setVisible(false);
                        var result = task.getValue();
                        if (result.success()) {
                            credentialStorage.save(CredentialType.GITHUB_TOKEN, result.accessToken());
                            loginButton.setVisible(false);
                            logoutButton.setVisible(true);
                            userCodeArea.setVisible(false);
                            TokenValidator.AuthResult validate = tokenValidator.validate();
                            credentialStorage.save(CredentialType.GITHUB_LOGIN, validate.login());
                            isUserLoggedIn.set(true);
                            statusText.setText("Logged in as [%s]".formatted(credentialStorage.get(CredentialType.GITHUB_LOGIN).get()));
                            log.info("Successfully logged in as [{}].", credentialStorage.get(CredentialType.GITHUB_LOGIN));
                        } else {
                            statusText.setText(result.error());
                            log.warn("Login unsuccessful, error: {}", task.getException().getMessage());
                        }
                    }
                });
                task.setOnFailed(e2 -> {
                    loginButton.setVisible(true);
                    loginButton.setDisable(false);
                    log.warn("[{}] Login failed, error: {}", "1638_280726", task.getException().getMessage());
                    statusText.setText("Login failed, check if your Client ID is correct.");

                    userCodeArea.setVisible(false);
                    credentialStorage.clear();
                    clientIdSaved = false;
                });
                new Thread(task, "github-device-login").start();
            } else {
                clientIdArea.setVisible(true);
                loginButton.setDisable(true);
                statusText.setText("Waiting for Client ID...");
            }
        });
    }

    private void setupLogoutButtonAction() {
        logoutButton.setOnAction(e -> {
            log.info("Logged out.");
            statusText.setText("Logged out.");
            loginButton.setVisible(true);
            loginButton.setDisable(false);
            logoutButton.setVisible(false);
            credentialStorage.clear();
            isUserLoggedIn.set(false);
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

    private void setupSaveClientIdButton() {
        saveClientIdButton.setOnAction(e -> {
            credentialStorage.save(CredentialType.GITHUB_CLIENT_ID, clientIdField.getText());
            log.info("Client ID: [{}] has been saved.", clientIdField.getText());
            clientIdSaved = true;
            clientIdArea.setVisible(false);
            loginButton.setDisable(false);
            statusText.setText("Client ID saved, you can login to GH now.");
        });
    }

    private void setupVerificationLinkCopyButton() {
        copyVerificationLinkButton.setOnAction(e -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String trimmed = verificationLink.trim();
            StringSelection selection = new StringSelection(trimmed);
            clipboard.setContents(selection, null);
            log.info("Verification link [{}] has been copied.", trimmed);
        });
    }
}
