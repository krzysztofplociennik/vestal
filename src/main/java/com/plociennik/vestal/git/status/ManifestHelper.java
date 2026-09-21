package com.plociennik.vestal.git.status;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.git.util.FilesUtils;
import com.plociennik.vestal.git.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

// todo: to be improved, string splitting could produce an error when f.e. the file is edited

@Slf4j
public class ManifestHelper {

    private AppConfigManager configManager = AppConfigManager.getInstance();
    private static final String MANIFEST_FILE_NAME = ".status-manifest";
    private static final String SEPARATOR = "|||";

    public ManifestHelper() {
        if (isManifestFileNotPresent()) {
            init();
        }
    }

    boolean isManifestEmpty() {
        boolean manifestFileNotPresent = isManifestFileNotPresent();
        if (manifestFileNotPresent) {
            throw new VestalException("1640_21092026", "The manifest file is missing.");
        } else {
            File manifestFile = getManifestFile();
            List<String> fileContents = FilesUtils.getFileContents(manifestFile);
            if (fileContents.isEmpty()) {
                return true;
            }

            StringBuilder fileContentsMerged = new StringBuilder();
            fileContents.forEach(l -> {
                String trimmedLine = l.trim();
                fileContentsMerged.append(trimmedLine);
            });

            String trimmedResult = fileContentsMerged.toString().trim();
            return org.apache.commons.lang3.StringUtils.isBlank(trimmedResult);
        }
    }

    File getManifestFile() {
        String destinationPath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath + "/" + MANIFEST_FILE_NAME;
        return new File(destinationPath);
    }

    boolean isManifestFileNotPresent() {
        File manifestFile = getManifestFile();
        return !manifestFile.exists();
    }

    Map<String, String> getManifestMap() {
        File manifestFile = getManifestFile();
        if (!manifestFile.exists()) {
            throw new VestalException("1334_26082026", "The status manifest file has not been created.");
        }

        Map<String, String> lastSavedMap = new HashMap<>();
        try (Scanner scanner = new Scanner(manifestFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int indexOfSeparator = StringUtils.indexOf(SEPARATOR, line);
                String pathSubString = line.substring(0, indexOfSeparator);
                String hashSubString = line.substring(indexOfSeparator + SEPARATOR.length());
                lastSavedMap.put(pathSubString, hashSubString);
            }
        } catch (FileNotFoundException e) {
            throw new VestalException("1406_26082026", "The file has not been found on the path: [%s].". formatted(manifestFile.getPath()), e);
        }

        return lastSavedMap;
    }

    Map<String, String> createCurrentFilenamesHashesMap() {
        Path path = Path.of(configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath);
        List<Path> filesFromPath = FilesUtils.collectFrom(path);

        Map<String, String> filenamesAndHashes = new HashMap<>();

        filesFromPath.stream()
                .filter(p -> !p.endsWith(MANIFEST_FILE_NAME))
                .forEach(p -> filenamesAndHashes.put(p.toString(), FilesUtils.hash(p)));

        return filenamesAndHashes;
    }

    String parseMapIntoString(Map<String, String> fileHashes) {
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

    private void init() {
        log.info("[{}] Checking if the status manifest is present.", "1419_21082026");

        File manifestFile = getManifestFile();
        if (manifestFile.exists()) {
            log.info("[{}] The file is present, cancelling the process.", "1419_21082026");
        } else {
            log.info("[{}] The file is missing, creating it now.", "1416_21082026");
            Path manifestPath = Path.of(manifestFile.getPath());
            Map<String, String> filenamesAndHashes = createCurrentFilenamesHashesMap();
            String fileContents = parseMapIntoString(filenamesAndHashes);
            try {
                Files.writeString(manifestPath, fileContents, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new VestalException("1443_21082026", "Something happened while trying to create the status manifest file.", e);
            }
            log.info("[{}] The manifest has been created at the path: [{}].", "1417_21082026", manifestFile.toPath());
        }
    }
}
