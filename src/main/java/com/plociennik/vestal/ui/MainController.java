package com.plociennik.vestal.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to Vestal!");
    }
}
