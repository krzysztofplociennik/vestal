package com.plociennik.vestal.login;

import com.github.javakeyring.Keyring;
import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

// todo: obviously, this is just for the time being, these creds need to be stored safely
//@Slf4j
//public class CredentialStorage {
//
//    private static final String PREF_KEY_GITHUB_CLIENT_ID = "github_client_id";
//    private static final String PREF_KEY_GITHUB_LOGIN = "github_login";
//    private static final String PREF_KEY_GITHUB_TOKEN = "github_access_token";
//    private final Preferences prefs = Preferences.userNodeForPackage(CredentialStorage.class);
//
//    // todo: this code needs to change
//    public Optional<String> load(CredentialType type) {
//
//        return switch (type) {
//            case GITHUB_CLIENT_ID -> Optional.ofNullable(prefs.get(PREF_KEY_GITHUB_CLIENT_ID, null));
//            case GITHUB_LOGIN -> Optional.ofNullable(prefs.get(PREF_KEY_GITHUB_LOGIN, null));
//            case GITHUB_TOKEN -> Optional.ofNullable(prefs.get(PREF_KEY_GITHUB_TOKEN, null));
//        };
//    }
//
//    public String get(CredentialType type) {
//        return load(type).orElseThrow(() -> new VestalException(
//                "1253_040826", "Credential type of [%s] is actually empty.".formatted(type)
//        ));
//    }
//
//    public void save(CredentialType type, String value) {
//        switch (type) {
//            case GITHUB_CLIENT_ID -> prefs.put(PREF_KEY_GITHUB_CLIENT_ID, value);
//            case GITHUB_LOGIN -> prefs.put(PREF_KEY_GITHUB_LOGIN, value);
//            case GITHUB_TOKEN -> prefs.put(PREF_KEY_GITHUB_TOKEN, value);
//        }
//    }
//
//    public void clear() {
//        try {
//            prefs.clear();
//        } catch (BackingStoreException e) {
//            log.error("[{}] Something happened while trying to clear all credentials from the storage: [error: {}]", "1414_280726", e.getMessage());
//            throw new VestalException("1414_280726", "Something happened while trying to clear all credentials from the storage: ", e);
//        }
//    }
//}
