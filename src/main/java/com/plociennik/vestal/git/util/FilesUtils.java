package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.OperatingSystem;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Slf4j
public class FilesUtils {

    public static String hash(Path file) {
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

    public static List<Path> collectFrom(Path path) {
        List<Path> files = new ArrayList<>();

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName() != null && dir.getFileName().toString().equals(".git")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    log.info("Visiting: {}", file);
                    files.add(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new VestalException("1148_19082026", "Something happened while trying to collect files.", e);
        }
        return files;
    }

    public static void write(Path path, String fileContents) {
        try {
            Files.writeString(path, fileContents, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new VestalException(
                    "1443_21082026",
                    "Something happened while trying to create a file of path: [%s].".formatted(path.toString()),
                    e);
        }
    }

    public static void write(Path path, byte[] fileContents) {
        try {
            Files.write(path, fileContents, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new VestalException(
                    "1444_21082026",
                    "Something happened while trying to create a file of path: [%s].".formatted(path.toString()),
                    e);
        }
    }

    public static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new VestalException(
                    "1326_31082026",
                    "Something happened while trying to delete a file of the path: [%s].".formatted(path.toString()),
                    e);
        }
    }

    public static String getOperatingSystemFolderSeparator() {
        OperatingSystem operatingSystem = AppConfigManager.getInstance().getCurrentConfig().operatingSystem;
        return switch (operatingSystem) {
            case LINUX -> "/";
            case WINDOWS -> "\\";
        };
    }
}
