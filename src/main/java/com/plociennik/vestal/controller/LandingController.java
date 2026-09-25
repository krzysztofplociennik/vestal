package com.plociennik.vestal.controller;

import com.plociennik.vestal.state.State;
import com.plociennik.vestal.state.StateService;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class LandingController extends VBox implements Initializable {

    public Button loginButton;

    private StateService stateService = StateService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupLoginButton();
    }

    private void setupLoginButton() {
        loginButton.setOnAction(e -> {
            stateService.setState(State.LOGIN);
        });
    }
}
