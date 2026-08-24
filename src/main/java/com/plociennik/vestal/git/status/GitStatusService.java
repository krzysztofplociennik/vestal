package com.plociennik.vestal.git.status;

// todo: generally the package should be somewhere else since it's not really using git
// todo: review

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfigManager;
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
import java.util.*;

@Slf4j
public class GitStatusService {

    private static final String MANIFEST_FILE_NAME = ".status-manifest";
    private static final String SEPARATOR = "||";

    private AppConfigManager configManager = AppConfigManager.getInstance();

    public void checkChanges() {
        // fetch manifest
        // create a map out of file
        // create a map out of directory
        // compare maps
    }

    public void init() {
        log.info("[{}] Checking if the status manifest is present.", "1419_21082026");

        String sourcePath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath;
        String destinationPath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath + "/" + MANIFEST_FILE_NAME;
        File manifestFile = new File(destinationPath);
        if (manifestFile.exists()) {
            log.info("[{}] The file is present, cancelling the process.", "1419_21082026");
        } else {
            log.info("[{}] The file is missing, creating it now.", "1416_21082026");
            Map<String, String> filenamesAndHashes = createFilenamesHashesMap(Path.of(sourcePath));
            Path manifestPath = Path.of(destinationPath);
            String fileContents = parseMapIntoString(filenamesAndHashes);
            try {
                Files.writeString(manifestPath, fileContents, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new VestalException("1443_21082026", "Something happened while trying to create the status manifest file.", e);
            }
            log.info("[{}] The manifest has been created at the path: [{}].", "1417_21082026", manifestFile.toPath());
        }
    }

    private Map<String, String> createFilenamesHashesMap(Path path) {
        Map<String, String> filenamesAndHashes = new HashMap<>();

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
                    if (file.endsWith(MANIFEST_FILE_NAME)) {
                        return FileVisitResult.CONTINUE;
                    }
                    filenamesAndHashes.put(file.toString(), hashFile(file));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new VestalException("1452_21082026", "Something happened while trying to create a filenames/hashes map.", e);
        }
        return filenamesAndHashes;
    }

    private String hashFile(Path file) {
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

    private String parseMapIntoString(Map<String, String> fileHashes) {
        Map<String, String> sorted = new TreeMap<>(fileHashes);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append(entry.getKey())
                    .append(SEPARATOR)
                    .append(entry.getValue())
                    .append('\n');
        }
        return sb.toString();
    }
}