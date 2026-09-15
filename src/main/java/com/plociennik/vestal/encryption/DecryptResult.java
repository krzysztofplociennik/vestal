package com.plociennik.vestal.encryption;

import java.nio.file.Path;

public record DecryptResult(Path destinationFile, byte[] content, String originalName) {}
