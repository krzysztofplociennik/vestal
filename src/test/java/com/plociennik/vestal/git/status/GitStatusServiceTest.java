package com.plociennik.vestal.git.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitStatusServiceTest {

    @Mock
    private ManifestHelper manifestHelper;

    @InjectMocks
    private GitStatusService gitStatusService;

    @Test
    void isStatusClean_returnsTrue_whenCurrentAndManifestMapsAreIdentical() {
        // Arrange
        Map<String, String> currentMap = Map.of("file1.txt", "hash1", "file2.txt", "hash2");
        Map<String, String> manifestMap = Map.of("file1.txt", "hash1", "file2.txt", "hash2");

        when(manifestHelper.createCurrentFilenamesHashesMap()).thenReturn(currentMap);
        when(manifestHelper.getManifestMap()).thenReturn(manifestMap);

        // Act
        boolean isStatusClean = gitStatusService.isClean();

        // Assert
        Assertions.assertTrue(isStatusClean);
    }

    @Test
    void isStatusClean_returnsFalse_whenOneValueIsDifferent() {
        // Arrange
        Map<String, String> currentMap = Map.of("file1.txt", "hash1", "file2.txt", "hash2");
        Map<String, String> manifestMap = Map.of("file1.txt", "hash1[different]", "file2.txt", "hash2");

        when(manifestHelper.createCurrentFilenamesHashesMap()).thenReturn(currentMap);
        when(manifestHelper.getManifestMap()).thenReturn(manifestMap);

        // Act
        boolean isStatusClean = gitStatusService.isClean();

        // Assert
        Assertions.assertFalse(isStatusClean);
    }

    @Test
    void isStatusClean_returnsFalse_whenMapsAreOfDifferentSizes() {
        // Arrange
        Map<String, String> currentMap = Map.of("file1.txt", "hash1", "file2.txt", "hash2");
        Map<String, String> manifestMap = Map.of("file1.txt", "hash1");

        when(manifestHelper.createCurrentFilenamesHashesMap()).thenReturn(currentMap);
        when(manifestHelper.getManifestMap()).thenReturn(manifestMap);

        // Act
        boolean isStatusClean = gitStatusService.isClean();

        // Assert
        Assertions.assertFalse(isStatusClean);
    }

    @Test
    void isStatusClean_returnsFalse_whenOneKeyIsDifferent() {
        // Arrange
        Map<String, String> currentMap = Map.of("file1[different].txt", "hash1", "file2.txt", "hash2");
        Map<String, String> manifestMap = Map.of("file1.txt", "hash1", "file2.txt", "hash2");

        when(manifestHelper.createCurrentFilenamesHashesMap()).thenReturn(currentMap);
        when(manifestHelper.getManifestMap()).thenReturn(manifestMap);

        // Act
        boolean isStatusClean = gitStatusService.isClean();

        // Assert
        Assertions.assertFalse(isStatusClean);
    }

}