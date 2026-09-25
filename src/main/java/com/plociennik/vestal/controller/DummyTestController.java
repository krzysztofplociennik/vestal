package com.plociennik.vestal.controller;

import com.plociennik.vestal.state.State;
import com.plociennik.vestal.state.StateService;
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

    private StateService stateService = StateService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML public Button loggedOutButton;
    @FXML private Button loginButton;
    @FXML public Button encryptionButton;
    @FXML private Button reposButton;
    @FXML private Button actionsButton;

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        stateService.setState(State.LOGGED_OUT);
        log.info("[{}] Test: pressing logged out button.","0948_25092026");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        stateService.setState(State.LOGIN);
        log.info("[{}] Test: pressing login button.","0945_25092026");
    }

    @FXML
    public void handleEncryption(ActionEvent actionEvent) {
        stateService.setState(State.ENCRYPTION_KEY);
        log.info("[{}] Test: pressing encryption button.","0949_25092026");
    }

    @FXML
    private void handleRepos(ActionEvent event) {
        stateService.setState(State.REPOS);
        log.info("[{}] Test: pressing repos button.","0946_25092026");
    }

    @FXML
    private void handleActions(ActionEvent event) {
        stateService.setState(State.ACTIONS);
        log.info("[{}] Test: pressing actions button.","0947_25092026");
    }
}
