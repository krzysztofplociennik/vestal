package com.plociennik.vestal.controller;

import com.plociennik.vestal.security.CredentialType;
import com.plociennik.vestal.security.CredentialsStorage;
import com.plociennik.vestal.security.KeyringCredentialsStorage;
import com.plociennik.vestal.state.State;
import com.plociennik.vestal.state.StateService;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class EncryptionKeyController extends VBox implements Initializable {

    public VBox encryptionKeyArea;
    public PasswordField encryptionKeyField;
    public Button encryptionKeyButton;

    private StateService stateService = StateService.getInstance();
    private CredentialsStorage credentialsStorage = new KeyringCredentialsStorage();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEncryptionKeyButton();
    }

    public void setupEncryptionKeyButton() {
        encryptionKeyButton.setOnAction(e -> {
            String encryptionKeyInput = encryptionKeyField.getText();
            credentialsStorage.save(CredentialType.ENCRYPTION_SECRET_KEY, encryptionKeyInput);
            stateService.setState(State.REPOS);
        });
    }
}
