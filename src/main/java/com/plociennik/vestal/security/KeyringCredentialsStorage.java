package com.plociennik.vestal.security;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

// todo: more logging

@Slf4j
public class KeyringCredentialsStorage implements CredentialsStorage {

    private static final String VESTAL_SERVICE_NAME = "com.plociennik.vestal";

    public KeyringCredentialsStorage() {
        // todo: hardcoded encryption key for simplicity right now; will be properly implemented
        save(CredentialType.ENCRYPTION_SECRET_KEY, "secret");
    }

    @Override
    public void save(CredentialType type, String value) {
        try (Keyring keyring = Keyring.create()) {
            keyring.setPassword(VESTAL_SERVICE_NAME, type.name(), value);
        } catch (BackendNotSupportedException e) {
            throw new IllegalStateException("No OS credential store available on this system.", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> load(CredentialType type) {
        try (Keyring keyring = Keyring.create()) {
            return Optional.of(keyring.getPassword(VESTAL_SERVICE_NAME, type.name()));
        } catch (BackendNotSupportedException | PasswordAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(CredentialType type) {
        Optional<String> optionalCredential = load(type);
        if (optionalCredential.isEmpty()) {
            log.error("[{}] Credential type of [{}] is actually empty.", "1253_040826", type);
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
        } catch (BackendNotSupportedException ignored) {
            log.warn("[{}] This OS does not support keyring implementation.", "1505_040826");
        } catch (Exception e) {
            log.error(
                    "[{}] Something happened while trying to clear all stored passwords, error: [{}]",
                    "1506_040826", e.toString());
            throw new VestalException(
                    "1506_040826", "Something happened while trying to clear all stored passwords.", e);
        }
    }
}
