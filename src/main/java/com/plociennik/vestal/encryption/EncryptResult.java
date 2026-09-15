package com.plociennik.vestal.encryption;

import java.nio.file.Path;

public record EncryptResult(Path fileDestination, String[] fileEncryptedPath, byte[] fileOutput) {}
