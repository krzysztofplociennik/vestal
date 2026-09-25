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

    @FXML private Button loginButton;
    @FXML private Button reposButton;
    @FXML private Button actionsButton;

    @FXML
    private void handleLogin(ActionEvent event) {
        stateService.setState(State.LOGIN);
        log.info("[{}] Test: pressing login button.","0945_25092026");
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
