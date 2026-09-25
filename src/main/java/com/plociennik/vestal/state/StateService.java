package com.plociennik.vestal.state;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfigManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StateService {

    private static StateService instance;

    private State currentState;

    private StateService() {
        this.currentState = AppConfigManager.getInstance().getCurrentConfig().vestalRepository.currentState;
    }

    public static StateService getInstance() {
        if (instance == null) {
            instance = new StateService();
        }
        return instance;
    }

    @Getter private BooleanProperty loginVisible = new SimpleBooleanProperty(false);
    @Getter private BooleanProperty repositoriesVisible = new SimpleBooleanProperty(false);
    @Getter private BooleanProperty actionsVisible = new SimpleBooleanProperty(false);

    public void setState(State state) {
        this.currentState = state;

        switch (state) {
            case LOGIN -> {
                loginVisible.setValue(true);
                repositoriesVisible.setValue(false);
                actionsVisible.setValue(false);
            }
            case REPOS -> {
                loginVisible.setValue(false);
                repositoriesVisible.setValue(true);
                actionsVisible.setValue(false);
            }
            case ACTIONS -> {
                loginVisible.setValue(false);
                repositoriesVisible.setValue(false);
                actionsVisible.setValue(true);
            }
            default -> throw new VestalException("1311_24092026", "State not recognized: [%s].".formatted(state));
        }
    }
}
