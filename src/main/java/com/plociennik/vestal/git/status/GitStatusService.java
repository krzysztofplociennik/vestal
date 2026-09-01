package com.plociennik.vestal.git.status;

// todo: generally the package should be somewhere else since it's not really using git

import com.plociennik.vestal.common.VestalException;
import com.plociennik.vestal.git.util.FilesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
public class GitStatusService {

    private ManifestHelper manifestHelper = new ManifestHelper();

    public boolean isClean() {
        if (manifestHelper.isManifestFileNotPresent()) {
            init();
        }
        Map<String, String> currentMap = manifestHelper.createCurrentFilenamesHashesMap();
        Map<String, String> manifestMap = manifestHelper.getManifestMap();

        Set<Map.Entry<String, String>> currentMapEntries = currentMap.entrySet();
        Set<Map.Entry<String, String>> manifestMapEntries = manifestMap.entrySet();
        if (currentMapEntries.size() != manifestMapEntries.size()) {
            return false;
        }

        Iterator<Map.Entry<String, String>> currentEntriesIterator = currentMapEntries.iterator();
        Iterator<Map.Entry<String, String>> manifestEntriesIterator = manifestMapEntries.iterator();

        Map.Entry<String, String> currentEntry = currentEntriesIterator.next();
        Map.Entry<String, String> manifestEntry = manifestEntriesIterator.next();

        while (currentEntry != null) {
            boolean hasCurrentEntry = checkIfMapHasMatchingEntry(currentEntry, manifestMap);
            if (!hasCurrentEntry) {
                return false;
            }
            boolean hasManifestEntry = checkIfMapHasMatchingEntry(manifestEntry, currentMap);
            if (!hasManifestEntry) {
                return false;
            }

            if (currentEntriesIterator.hasNext()) {
                currentEntry = currentEntriesIterator.next();
                manifestEntry = manifestEntriesIterator.next();
            } else {
                currentEntry = null;
            }
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

    private void init() {
        log.info("[{}] Checking if the status manifest is present.", "1419_21082026");

        File manifestFile = manifestHelper.getManifestFile();
        if (manifestFile.exists()) {
            log.info("[{}] The file is present, cancelling the process.", "1419_21082026");
        } else {
            log.info("[{}] The file is missing, creating it now.", "1416_21082026");
            Map<String, String> filenamesAndHashes = manifestHelper.createCurrentFilenamesHashesMap();
            Path manifestPath = Path.of(manifestFile.getPath());
            String fileContents = manifestHelper.parseMapIntoString(filenamesAndHashes);
            try {
                Files.writeString(manifestPath, fileContents, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new VestalException("1443_21082026", "Something happened while trying to create the status manifest file.", e);
            }
            log.info("[{}] The manifest has been created at the path: [{}].", "1417_21082026", manifestFile.toPath());
        }
    }

    public void updateManifest() {
        log.info("[{}] Status manifest is being updated.", "1335_31082026");
        File currentFile = manifestHelper.getManifestFile();
        Path manifestPath = currentFile.toPath();
        FilesUtils.delete(manifestPath);
        Map<String, String> currentManifestMap = manifestHelper.createCurrentFilenamesHashesMap();
        String fileContents = manifestHelper.parseMapIntoString(currentManifestMap);
        FilesUtils.write(manifestPath, fileContents);
        log.info("[{}] Status manifest has been successfully updated.", "1336_31082026");
    }
}