package com.plociennik.vestal.git;

import java.util.Optional;
import java.util.prefs.Preferences;

public class TokenStorage {

    private static final String PREF_KEY = "github_access_token";
    private final Preferences prefs = Preferences.userNodeForPackage(TokenStorage.class);

    public void save(String token) {
        prefs.put(PREF_KEY, token);
    }

    public Optional<String> load() {
        return Optional.ofNullable(prefs.get(PREF_KEY, null));
    }

    public void clear() {
        prefs.remove(PREF_KEY);
    }
}
