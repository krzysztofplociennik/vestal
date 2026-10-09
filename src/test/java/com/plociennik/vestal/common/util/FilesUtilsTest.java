package com.plociennik.vestal.common.util;

import com.plociennik.vestal.common.VestalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FilesUtilsTest {

    // --------------------------------------------
    // ---------- String hash(Path file) ----------
    // --------------------------------------------

    @Test
    @DisplayName("hash of an empty file matches the known SHA-256 of empty input")
    void hashEmptyFile(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("empty.txt");
        Files.createFile(file);

        // Act
        String actual = FilesUtils.hash(file);

        // Assert
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("hash of 'hello' matches the known SHA-256 of 'hello'")
    void hashFileWithHello(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("hello.txt");
        Files.writeString(file, "hello");

        // Act
        String actual = FilesUtils.hash(file);

        // Assert
        String expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("same bytes in different files → same hash (deterministic)")
    void deterministicAcrossFiles(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path a = tempDir.resolve("a.bin");
        Path b = tempDir.resolve("b.bin");
        Files.writeString(a, "same content");
        Files.writeString(b, "same content");

        // Act
        String hashA = FilesUtils.hash(a);
        String hashB = FilesUtils.hash(b);

        // Assert
        assertEquals(hashA, hashB);
    }

    @Test
    @DisplayName("different bytes → different hash")
    void differentContentDifferentHash(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path a = tempDir.resolve("a.bin");
        Path b = tempDir.resolve("b.bin");
        Files.writeString(a, "alpha");
        Files.writeString(b, "beta");

        // Act
        String hashA = FilesUtils.hash(a);
        String hashB = FilesUtils.hash(b);

        // Assert
        assertNotEquals(hashA, hashB);
    }

    @Nested
    @DisplayName("Error scenarios")
    class Errors {

        @Test
        @DisplayName("missing file → VestalException 1510_21082026 with filename in message")
        void missingFile(@TempDir Path tempDir) {
            // Arrange
            Path missing = tempDir.resolve("does-not-exist.bin");

            // Act Assert
            VestalException ex = assertThrows(VestalException.class,
                    () -> FilesUtils.hash(missing));

            assertEquals("1510_21082026", ex.getErrorID());
            assertTrue(ex.getMessage().contains("does-not-exist.bin"),
                    "Message should include the file name, was: " + ex.getMessage());
        }

        @Test
        @DisplayName("passing a directory → read failure → VestalException 1510_21082026")
        void directoryAsFile(@TempDir Path tempDir) {
            VestalException ex = assertThrows(VestalException.class,
                    () -> FilesUtils.hash(tempDir));

            assertEquals("1510_21082026", ex.getErrorID());
        }
    }


    // -------------------------------------------------------
    // ---------- List<Path> collectFrom(Path path) ----------
    // -------------------------------------------------------



    // ---------------------------------------
    // ---------- paste_method_here ----------
    // ---------------------------------------


    @Test
    void collectFrom() {
    }

    @Test
    void write() {
    }

    @Test
    void testWrite() {
    }

    @Test
    void delete() {
    }

    @Test
    void getOperatingSystemFolderSeparator() {
    }

    @Test
    void doesPathExist() {
    }

    @Test
    void createEmptyDirectory() {
    }

    @Test
    void getFile() {
    }

    @Test
    void getFileContents() {
    }
}