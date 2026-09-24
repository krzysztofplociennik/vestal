package com.plociennik.vestal.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class DummyTestController extends HBox implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML private Button loginButton;
    @FXML private Button reposButton;
    @FXML private Button actionsButton;

    @FXML
    private void initialize() {
        // You can initially disable downstream buttons until logged in
        // reposButton.setDisable(true);
        // actionsButton.setDisable(true);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        System.out.println("Opening GitHub Login step...");
    }

    @FXML
    private void handleRepos(ActionEvent event) {
        System.out.println("Opening Local & Remote Repository config...");
    }

    @FXML
    private void handleActions(ActionEvent event) {
        System.out.println("Opening Git sync actions (Status, Pull, Push)...");
    }
}
