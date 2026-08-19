package com.plociennik.vestal.git.util;

import com.plociennik.vestal.common.VestalException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FilesCollector {

    public static List<Path> from(Path path) {
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
}
