package com.plociennik.vestal.common.util;

import com.plociennik.vestal.common.VestalException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class FilesUtilsTest {

    // --------------------------------------------
    // ---------- String hash(Path file) ----------
    // --------------------------------------------

    @Test
    @DisplayName("hash of an empty file matches the known SHA-256 of empty input")
    void hash_EmptyFile(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("empty.txt");
        Files.createFile(file);

        // Act
        String actual = FilesUtils.hash(file);

        // Assert
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("hash of 'hello' matches the known SHA-256 of 'hello'")
    void hash_FileWithHello(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("hello.txt");
        Files.writeString(file, "hello");

        // Act
        String actual = FilesUtils.hash(file);

        // Assert
        String expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("same bytes in different files -> same hash (deterministic)")
    void hash_deterministicAcrossFiles(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path a = tempDir.resolve("a.bin");
        Path b = tempDir.resolve("b.bin");
        Files.writeString(a, "same content");
        Files.writeString(b, "same content");

        // Act
        String hashA = FilesUtils.hash(a);
        String hashB = FilesUtils.hash(b);

        // Assert
        Assertions.assertEquals(hashA, hashB);
    }

    @Test
    @DisplayName("different bytes -> different hash")
    void hash_differentContentDifferentHash(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path a = tempDir.resolve("a.bin");
        Path b = tempDir.resolve("b.bin");
        Files.writeString(a, "alpha");
        Files.writeString(b, "beta");

        // Act
        String hashA = FilesUtils.hash(a);
        String hashB = FilesUtils.hash(b);

        // Assert
        Assertions.assertNotEquals(hashA, hashB);
    }

    // ---------- Errors ----------

    @Nested
    @DisplayName("Error scenarios")
    class Errors {

        @Test
        @DisplayName("missing file -> VestalException 1510_21082026 with filename in message")
        void hash_missingFile(@TempDir Path tempDir) {
            // Arrange
            Path missing = tempDir.resolve("does-not-exist.bin");

            // Act Assert
            VestalException ex = Assertions.assertThrows(VestalException.class,
                    () -> FilesUtils.hash(missing));

            Assertions.assertEquals("1510_21082026", ex.getErrorID());
            Assertions.assertTrue(ex.getMessage().contains("does-not-exist.bin"),
                    "Message should include the file name, was: " + ex.getMessage());
        }

        @Test
        @DisplayName("passing a directory → read failure → VestalException 1510_21082026")
        void hash_directoryAsFile(@TempDir Path tempDir) {
            // Arrange Act Assert
            VestalException ex = Assertions.assertThrows(VestalException.class,
                    () -> FilesUtils.hash(tempDir));

            Assertions.assertEquals("1510_21082026", ex.getErrorID());
        }
    }

    // -------------------------------------------------------
    // ---------- List<Path> collectFrom(Path path) ----------
    // -------------------------------------------------------

    @Test
    @DisplayName("empty directory -> empty list")
    void collectFrom_emptyDirectory(@TempDir Path tempDir) {
        // Arrange Act
        List<Path> paths = FilesUtils.collectFrom(tempDir);

        // Assert
        Assertions.assertTrue(paths.isEmpty());
    }

    @Test
    @DisplayName("single file at root is collected")
    void collectFrom_singleFileAtRoot(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path f = tempDir.resolve("a.txt");
        Files.writeString(f, "hi");

        // Act
        List<Path> result = FilesUtils.collectFrom(tempDir);

        // Assert
        Assertions.assertEquals(List.of(f), result);
    }

    @Test
    @DisplayName("nested files are all collected (order-independent)")
    void collectFrom_nestedFiles(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path subDir = tempDir.resolve("sub");
        Path deepDir = subDir.resolve("deep");
        Path rootFile = tempDir.resolve("root.txt");
        Path midFile = subDir.resolve("mid.txt");
        Path leafFile = deepDir.resolve("leaf.txt");

        Files.createDirectories(subDir);
        Files.createDirectories(deepDir);

        Files.writeString(rootFile, "r");
        Files.writeString(midFile, "m");
        Files.writeString(leafFile, "l");

        // Act
        List<Path> result = FilesUtils.collectFrom(tempDir);

        // Assert
        Assertions.assertEquals(Set.of("root.txt", "sub/mid.txt", "sub/deep/leaf.txt"),
                relativize(result, tempDir));
        Assertions.assertEquals(3, result.size());
    }

    // ---------- .git handling ----------

    @Nested
    @DisplayName(".git directory handling")
    class GitHandling {

        @Test
        @DisplayName(".git at root is skipped entirely (its contents are not collected)")
        void collectFrom_gitAtRootIsSkipped(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path gitDir = tempDir.resolve(".git");
            Path objectsDir = gitDir.resolve("objects");
            Path configFile = gitDir.resolve("config.ini");
            Path packFile = objectsDir.resolve("pack.dat");
            Path realFile = tempDir.resolve("real.txt");

            Files.createDirectories(tempDir);
            Files.createDirectories(gitDir);
            Files.createDirectories(objectsDir);

            Files.writeString(packFile, "binary");
            Files.writeString(configFile, "core.bare=false");
            Files.writeString(realFile, "keep me");

            // Act
            List<Path> result = FilesUtils.collectFrom(tempDir);

            // Assert
            Assertions.assertEquals(Set.of("real.txt"), relativize(result, tempDir));
        }

        @Test
        @DisplayName(".git nested inside another directory is also skipped")
        void collectFrom_nestedGitIsSkipped(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path projectDir = tempDir.resolve("project");
            Path gitDir = projectDir.resolve(".git");
            Path headFile = gitDir.resolve("HEAD");
            Path projectFile = projectDir.resolve("src.txt");

            Files.createDirectories(gitDir);

            Files.writeString(headFile, "ref: refs/heads/main");
            Files.writeString(projectFile, "keep");

            // Act
            List<Path> result = FilesUtils.collectFrom(tempDir);

            // Assert
            Assertions.assertEquals(Set.of("project/src.txt"), relativize(result, tempDir));
        }

        @Test
        @DisplayName("a FILE named '.git' is NOT skipped (only directories are)")
        void collectFrom_fileNamedGitIsKept(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path dotGitFile = tempDir.resolve(".git");
            Files.writeString(dotGitFile, "I am a file");

            // Act
            List<Path> result = FilesUtils.collectFrom(tempDir);

            // Assert
            Assertions.assertEquals(List.of(dotGitFile), result);
        }

        @Test
        @DisplayName("lookalikes like '.github', 'my.git', '.git2' are NOT skipped")
        void collectFrom_similarNamesAreNotSkipped(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path lookalikeDir1 = tempDir.resolve(".github");
            Path lookalikeDir2 = tempDir.resolve("my.git");
            Path lookalikeDir3 = tempDir.resolve(".git2");
            Path file1 = lookalikeDir1.resolve("f1");
            Path file2 = lookalikeDir2.resolve("f2");
            Path file3 = lookalikeDir3.resolve("f3");

            Files.createDirectories(lookalikeDir1);
            Files.createDirectories(lookalikeDir2);
            Files.createDirectories(lookalikeDir3);

            Files.writeString(file1, "1");
            Files.writeString(file2, "2");
            Files.writeString(file3, "3");

            // Act
            List<Path> result = FilesUtils.collectFrom(tempDir);

            // Assert
            Assertions.assertEquals(Set.of(".github/f1", "my.git/f2", ".git2/f3"),
                    relativize(result, tempDir));
        }
    }

    private static Set<String> relativize(List<Path> files, Path root) {
        return files.stream()
                .map(p -> root.relativize(p).toString().replace('\\', '/'))
                .collect(Collectors.toSet());
    }

    // ----------------------------------------------------------------
    // ---------- void write(Path path, byte[] fileContents) ----------
    // ----------------------------------------------------------------

    @Test
    @DisplayName("write: writes exact bytes to an existing directory")
    void write_existingDirectory(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("out.bin");
        byte[] payload = {1, 2, 3, 4, 5};

        // Act
        FilesUtils.write(target, payload);

        // Assert
        Assertions.assertTrue(Files.isRegularFile(target));
        Assertions.assertArrayEquals(payload, Files.readAllBytes(target));
    }

    @Test
    @DisplayName("write: empty byte array creates a zero-length file")
    void write_emptyPayload(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("empty.bin");
        byte[] payload = new byte[0];

        // Act
        FilesUtils.write(target, payload);

        // Assert
        Assertions.assertTrue(Files.isRegularFile(target));
        Assertions.assertEquals(0L, Files.size(target));
    }

    @Test
    @DisplayName("write: binary content (all 256 byte values) is preserved exactly")
    void write_binaryContentPreserved(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("bin.dat");
        byte[] payload = new byte[256];
        for (int i = 0; i < 256; i++) payload[i] = (byte) i;

        // Act
        FilesUtils.write(target, payload);

        // Assert
        Assertions.assertArrayEquals(payload, Files.readAllBytes(target));
    }

    @Test
    @DisplayName("write: text content is readable back as the same string")
    void write_textRoundTrip(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("note.txt");
        String text = "Hello, World!";
        byte[] payload = text.getBytes();

        // Act
        FilesUtils.write(target, payload);

        // Assert
        Assertions. assertEquals(text, Files.readString(target));
    }

    @Test
    @DisplayName("write: second write to the same path succeeds")
    void write_secondWriteSucceeds(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("x.txt");

        // Act
        FilesUtils.write(target, "first".getBytes());
        FilesUtils.write(target, "second".getBytes());

        // Assert
        Assertions.assertEquals("second", Files.readString(target));
    }

    @Test
    @DisplayName("write: writing shorter content over longer must truncate (no stale tail)")
    void write_shorterOverLongerMustTruncate(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("x.txt");

        // Act
        FilesUtils.write(target, "hello world".getBytes());
        FilesUtils.write(target, "hi".getBytes());

        // Assert
        byte[] withStaleTail = "hillo world".getBytes();
        byte[] actual = Files.readAllBytes(target);
        byte[] expected = "hi".getBytes();

        Assertions.assertNotEquals(withStaleTail, actual);
        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    @DisplayName("write: creates a single missing parent directory")
    void write_createsMissingDirs_singleLevel(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("sub").resolve("out.bin");
        Assertions.assertFalse(Files.exists(target.getParent()));

        // Act
        FilesUtils.write(target, "data".getBytes());

        // Assert
        Assertions.assertTrue(Files.exists(target.getParent()));
        Assertions.assertTrue(Files.isDirectory(tempDir.resolve("sub")));
        Assertions.assertTrue(Files.isRegularFile(target));
        Assertions.assertEquals("data", Files.readString(target));
    }

    @Test
    @DisplayName("write: creates multiple missing parent directories (deep path)")
    void write_createsMissingDirs_deepPath(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path target = tempDir.resolve("a").resolve("b").resolve("c")
                .resolve("d").resolve("out.bin");
        Assertions.assertFalse(Files.exists(tempDir.resolve("a")));
        Assertions.assertFalse(Files.exists(tempDir.resolve("a").resolve("b")));
        Assertions.assertFalse(Files.exists(tempDir.resolve("a").resolve("b").resolve("c")));

        // Act
        FilesUtils.write(target, "deep".getBytes());

        // Assert
        Assertions.assertTrue(Files.isDirectory(tempDir.resolve("a")));
        Assertions.assertTrue(Files.isDirectory(tempDir.resolve("a").resolve("b")));
        Assertions.assertTrue(Files.isDirectory(tempDir.resolve("a").resolve("b").resolve("c")));
        Assertions.assertTrue(Files.isDirectory(target.getParent()));
        Assertions.assertEquals("deep", Files.readString(target));
    }

    @Test
    @DisplayName("write: does NOT fail when parent directories already exist")
    void write_createsMissingDirs_alreadyExist(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path parent = tempDir.resolve("existing").resolve("dir");
        Files.createDirectories(parent);
        Path target = parent.resolve("out.bin");

        // Act
        FilesUtils.write(target, "ok".getBytes());

        // Assert
        Assertions.assertEquals("ok", Files.readString(target));
    }

    // ---------------------------------------
    // ---------- paste_method_here ----------
    // ---------------------------------------

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