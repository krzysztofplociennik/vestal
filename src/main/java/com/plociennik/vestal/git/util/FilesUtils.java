package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.config.OperatingSystem;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Scanner;

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
            createMissingDirectories(path);
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
            createMissingDirectories(path);
            Files.write(path, fileContents, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new VestalException(
                    "1444_21082026",
                    "Something happened while trying to create a file of path: [%s].".formatted(path.toString()),
                    e);
        }
    }

    // todo: test
    private static void createMissingDirectories(Path path) {
        String separator = getOperatingSystemFolderSeparator();
        // todo: method to be implemented in CustomStringUtils
        int lastSeparator = path.toString().lastIndexOf(separator);
        String substring = CustomStringUtils.substring(path.toString(), 0, lastSeparator);
        try {
            Files.createDirectories(Path.of(substring));
        } catch (IOException e) {
            throw new VestalException(
                    "1936_22092026",
                    "Something happened while trying to create missing directories from path: [%s]"
                            .formatted(path.toString()),
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

    public static boolean doesPathExist(String path) {
        return Files.exists(Path.of(path));
    }

    public static void createEmptyDirectory(String path, String name) {
        Path directory = Path.of(path);
        Path folder = directory.resolve(name);
        try {
            Files.createDirectory(folder);
        } catch (IOException e) {
            throw new VestalException("1354_09092026", "Something happened while trying to create an empty folder.", e);
        }
    }

    public static File getFile(Path path) {
        boolean b = doesPathExist(path.toString());
        if (!b) {
            throw new VestalException("1651_21092026", "The file on the path: [%s] is missing.".formatted(path.toString()));
        }
        return new File(path.toString());
    }

    public static List<String> getFileContents(File file) {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lines.add(line);
            }
        } catch (Exception e) {
            throw new VestalException("1656_21092026", "Something happened while trying to read file contents.", e);
        }
        return lines;
    }
}
