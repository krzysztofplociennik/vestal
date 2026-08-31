package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FileHasher {

    public static String hashFile(Path file) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new VestalException("1509_21082026", "Something happened while trying to create a digest.", e);
        }
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(file);
        } catch (IOException e) {
            throw new VestalException("1510_21082026", "Something happened while trying to read the file of the name: [%s]". formatted(file.getFileName()));
        }
        byte[] hashBytes = digest.digest(fileBytes);
        return HexFormat.of().formatHex(hashBytes);
    }
}
