package com.plociennik.vestal.git.status;

// todo: generally the package should be somewhere else since it's not really using git
// todo: the name probably should be just changed

import com.plociennik.vestal.git.util.FilesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
public class GitStatusService {

    private ManifestHelper manifestHelper = new ManifestHelper();

    public boolean isClean() {
        boolean manifestEmpty = manifestHelper.isManifestEmpty();
        if (manifestEmpty) {
            // todo: or potentially send info to global bar that there are no items locally now
            return true;
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

    public void updateManifest() {
        log.info("[{}] Status manifest is being updated.", "1335_31082026");
        File currentFile = manifestHelper.getManifestFile();
        Path manifestPath = currentFile.toPath();
        boolean exists = currentFile.exists();
        if (exists) {
            FilesUtils.delete(manifestPath);
        }
        Map<String, String> currentManifestMap = manifestHelper.createCurrentFilenamesHashesMap();
        String fileContents = manifestHelper.parseMapIntoString(currentManifestMap);
        FilesUtils.write(manifestPath, fileContents);
        log.info("[{}] Status manifest has been successfully updated.", "1336_31082026");
    }
}