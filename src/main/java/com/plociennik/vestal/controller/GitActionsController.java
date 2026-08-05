package com.plociennik.vestal.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class GitActionsController extends VBox implements Initializable {

    @FXML private Text statusLabelText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabelText.setText("Initial state.");
    }
}
