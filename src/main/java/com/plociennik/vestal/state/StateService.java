package com.plociennik.vestal.state;

import com.plociennik.vestal.config.AppConfigManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static java.util.Map.entry;

@Slf4j
public class StateService {

    private static StateService instance;

    private Map<State, BooleanProperty> mapOfStatesAndProperties;

    private StateService() {
        initMap();
    }

    public static StateService getInstance() {
        if (instance == null) {
            instance = new StateService();
        }
        return instance;
    }

    @Getter private BooleanProperty landingVisible = new SimpleBooleanProperty(false);
    @Getter private BooleanProperty loginVisible = new SimpleBooleanProperty(false);
    @Getter private BooleanProperty encryptionKeyVisible = new SimpleBooleanProperty(false);
    @Getter private BooleanProperty repositoriesVisible = new SimpleBooleanProperty(false);
    @Getter private BooleanProperty actionsVisible = new SimpleBooleanProperty(false);

    @Getter private BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

    public void setState(State state) {
        mapOfStatesAndProperties.forEach((key, value) -> value.setValue(false));
        mapOfStatesAndProperties.get(state).setValue(true);
        isLoggedIn.setValue(state.getOrder() > 2);
        AppConfigManager manager = AppConfigManager.getInstance();
        manager.update(ac -> ac.vestalRepository.state = state);
    }

    private void initMap() {
        this.mapOfStatesAndProperties = Map.ofEntries(
                entry(State.LOGGED_OUT, landingVisible),
                entry(State.LOGIN, loginVisible),
                entry(State.ENCRYPTION_KEY, encryptionKeyVisible),
                entry(State.REPOS, repositoriesVisible),
                entry(State.ACTIONS, actionsVisible)
        );
    }

    // todo: readState
    // todo: saveState
    // todo: go next state
}
