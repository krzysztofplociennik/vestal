package com.plociennik.vestal.security;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class KeyringCredentialsStorage implements CredentialsStorage {

    private static final String VESTAL_SERVICE_NAME = "com.plociennik.vestal";

    public KeyringCredentialsStorage() {
        // todo: hardcoded encryption key for simplicity right now; will be properly implemented
        Optional<String> optionalSecretKey = load(CredentialType.ENCRYPTION_SECRET_KEY);
        if (optionalSecretKey.isEmpty()) {
            save(CredentialType.ENCRYPTION_SECRET_KEY, "secret");
        }
    }

    @Override
    public void save(CredentialType type, String value) {
        try (Keyring keyring = Keyring.create()) {
            keyring.setPassword(VESTAL_SERVICE_NAME, type.name(), value);
        } catch (BackendNotSupportedException e) {
            throw new VestalException("1118_07092026", "This OS does not support keyring implementation.", e);
        } catch (Exception e) {
            throw new VestalException(
                    "1117_07092026",
                    "Something happened while trying to save credentials of type: [%s]".formatted(type),
                    e);
        }
    }

    @Override
    public Optional<String> load(CredentialType type) {
        try (Keyring keyring = Keyring.create()) {
            return Optional.of(keyring.getPassword(VESTAL_SERVICE_NAME, type.name()));
        } catch (Exception e) {
            throw new VestalException(
                    "1115_07092026",
                    "Something happened while trying to retrieve credentials of type: [%s].".formatted(type),
                    e);
        }
    }

    @Override
    public String get(CredentialType type) {
        Optional<String> optionalCredential = load(type);
        if (optionalCredential.isEmpty()) {
            throw new VestalException("1253_040826", "Credential type of [%s] is actually empty.".formatted(type));
        }
        return optionalCredential.get();
    }

    @Override
    public void clear() {
        try (Keyring keyring = Keyring.create()) {
            for (CredentialType type : CredentialType.values()) {
                try {
                    keyring.deletePassword(VESTAL_SERVICE_NAME, type.name());
                    log.info(
                            "[{}] Password for the service [{}] of the type [{}] has been successfully deleted.",
                            "1511_040826", VESTAL_SERVICE_NAME, type);
                } catch (PasswordAccessException ignored) {
                    log.warn(
                            "[{}] There is no password stored for the type of [{}] for the service of [{}]",
                            "1504_040826", type, VESTAL_SERVICE_NAME);
                }
            }
        } catch (BackendNotSupportedException e) {
            throw new VestalException("1113_07092026", "This OS does not support keyring implementation.", e);
        } catch (Exception e) {
            throw new VestalException(
                    "1506_040826", "Something happened while trying to clear all stored passwords.", e);
        }
    }
}
