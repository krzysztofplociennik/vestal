package com.plociennik.vestal.config;

import lombok.Data;

import java.nio.file.Path;

@Data
public class AppConfig {

    public VestalRepository vestalRepository;
    public Path operatingSystemMainDirectory;
    public byte[] encryptionSalt;

    public AppConfig() {
        this.vestalRepository = new VestalRepository();
    }
}
