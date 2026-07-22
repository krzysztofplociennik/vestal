package com.plociennik.vestal.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class GithubLoginController extends HBox {

    @FXML
    private Button loginButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label loginStatus;

    @FXML
    protected void onLoginButtonClick() {
        loginButton.setVisible(false);
        logoutButton.setVisible(true);
        loginStatus.setText("logged in");
    }

    @FXML
    protected void onLogoutButtonClick() {
        loginButton.setVisible(true);
        logoutButton.setVisible(false);
        loginStatus.setText("not logged in");
    }
}
