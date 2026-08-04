package com.plociennik.vestal.login;

import java.util.Optional;

public interface CredentialsStorage {
    Optional<String> load(CredentialType type);
    String get(CredentialType type);
    void save(CredentialType type, String value);
    void clear();
}
