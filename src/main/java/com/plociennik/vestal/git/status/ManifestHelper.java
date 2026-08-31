package com.plociennik.vestal.git.status;

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.config.AppConfigManager;
import com.plociennik.vestal.git.util.FilesUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ManifestHelper {

    private AppConfigManager configManager = AppConfigManager.getInstance();
    private static final String MANIFEST_FILE_NAME = ".status-manifest";
    private static final String SEPARATOR = "||";

    File getManifestFile() {
        String destinationPath = configManager.getCurrentConfig().vestalRepository.localRepository.sourcePath + "/" + MANIFEST_FILE_NAME;
        return new File(destinationPath);
    }

    boolean isManifestFileNotPresent() {
        File manifestFile = getManifestFile();
        return manifestFile.exists();
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
                String[] split = line.split(SEPARATOR);
                lastSavedMap.put(split[0], split[1]);
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
}
