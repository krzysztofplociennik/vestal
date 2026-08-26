package com.plociennik.vestal.git.status;

// todo: generally the package should be somewhere else since it's not really using git
// todo: review

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfigManager;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

@Slf4j
public class GitStatusService {

    private static final String MANIFEST_FILE_NAME = ".status-manifest";
    private static final String SEPARATOR = "||";

    private AppConfigManager configManager = AppConfigManager.getInstance();

    // todo: needs to be tested
    public boolean isStatusChanged() {

        Map<String, String> currentMap = createCurrentFilenamesHashesMap();
        Map<String, String> manifestMap = getManifestMap();

        Set<Map.Entry<String, String>> currentMapEntries = currentMap.entrySet();
        Set<Map.Entry<String, String>> manifestMapEntries = manifestMap.entrySet();
        if (currentMapEntries.size() != manifestMapEntries.size()) {
            return false;
        }

        Iterator<Map.Entry<String, String>> currentEntriesIterator = currentMapEntries.iterator();
        Iterator<Map.Entry<String, String>> manifestEntriesIterator = manifestMapEntries.iterator();

        Map.Entry<String, String> currentEntry = currentEntriesIterator.next();
        Map.Entry<String, String> manifestEntry = manifestEntriesIterator.next();

        while (currentEntriesIterator.hasNext()) {
            boolean hasCurrentEntry = checkIfMapHasMatchingEntry(currentEntry, manifestMap);
            if (!hasCurrentEntry) {
                return false;
            }
            boolean hasManifestEntry = checkIfMapHasMatchingEntry(manifestEntry, currentMap);
            if (!hasManifestEntry) {
                return false;
            }
            currentEntry = currentEntriesIterator.next();
            manifestEntry = manifestEntriesIterator.next();
        }
        return true;
    }

    private boolean checkIfMapHasMatchingEntry(Map.Entry<String, String> currentEntry, Map<String, String> map) {

        String key = currentEntry.getKey();
        String value = currentEntry.getValue();

        if (!map.containsKey(key)) {
            return false;
        }
        String searchedValue = map.get(key);
        return searchedValue != null && searchedValue.equals(value);
    }

    private File getManifestFile() {
        String destinationPath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath + "/" + MANIFEST_FILE_NAME;
        return new File(destinationPath);
    }

    private Map<String, String> getManifestMap() {
        File manifestFile = getManifestFile();
        if (!manifestFile.exists()) {
            throw new VestalException("1334_26082026", "The status manifest file is not present.");
        }

        Map<String, String> lastSavedMap = new HashMap<>();
        try (Scanner scanner = new Scanner(manifestFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] split = line.split(SEPARATOR);
                lastSavedMap.put(split[0], split[1]);
            }
        } catch (FileNotFoundException e) {
            throw new VestalException("1406_26082026", "The file has not been found on the path: [%s].". formatted(manifestFile.getPath()), e);
        }

        return lastSavedMap;
    }

    public void init() {
        log.info("[{}] Checking if the status manifest is present.", "1419_21082026");

        File manifestFile = getManifestFile();
        if (manifestFile.exists()) {
            log.info("[{}] The file is present, cancelling the process.", "1419_21082026");
        } else {
            log.info("[{}] The file is missing, creating it now.", "1416_21082026");
            Map<String, String> filenamesAndHashes = createCurrentFilenamesHashesMap();
            Path manifestPath = Path.of(manifestFile.getPath());
            String fileContents = parseMapIntoString(filenamesAndHashes);
            try {
                Files.writeString(manifestPath, fileContents, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new VestalException("1443_21082026", "Something happened while trying to create the status manifest file.", e);
            }
            log.info("[{}] The manifest has been created at the path: [{}].", "1417_21082026", manifestFile.toPath());
        }
    }

    private Map<String, String> createCurrentFilenamesHashesMap() {
        Path path = Path.of(configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath);

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