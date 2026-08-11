package com.plociennik.vestal.controller;

import com.plociennik.vestal.git.push.RepoPushChangesService;
import com.plociennik.vestal.git.util.GitUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.net.URL;
import java.util.ResourceBundle;

import static com.plociennik.vestal.common.CommonUtils.logAndThrow;

@Slf4j
public class GitActionsController extends VBox implements Initializable {

    @FXML private Text statusLabelText;
    @FXML private Button checkStatusButton;
    @FXML private Button pushChangesButton;
    @FXML private Button pullChangesButton;

    private RepoPushChangesService repoPushChangesService = new RepoPushChangesService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabelText.setText("Initial state.");
        setupCheckStatusButton();
        setupPushChangesButton();
        setupPullChangesButton();
    }

    private void setupCheckStatusButton() {
        checkStatusButton.setOnAction(e -> {
            Repository gitRepository = GitUtils.getExistingLocalRepo();

            try (Git git = new Git(gitRepository)) {

                Status status = git.status().call();
                if (status.isClean()) {
                    statusLabelText.setText("There are no changes to be pushed.");
                } else {
                    statusLabelText.setText("There are changes to be pushed.");
                }

            } catch (GitAPIException error) {
                logAndThrow("1218_10082026", "Something happened when trying to check changes.", error);
            }

        });
    }

    private void setupPushChangesButton() {
        pushChangesButton.setOnAction(e -> {
            repoPushChangesService.push();
        });
    }

    private void setupPullChangesButton() {

    }
}
