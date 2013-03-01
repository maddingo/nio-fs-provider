package no.uis.nio.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class PathUtil {
  private PathUtil() {
  }
  
  public static void createDirectory(Path outDir, boolean cleanExisting) throws IOException {
    
    if (cleanExisting) {
      cleanDirectory(outDir);
    }
    Files.createDirectories(outDir);
  }
  
  private static void cleanDirectory(Path outDir) throws IOException {
    if (Files.isDirectory(outDir) && Files.exists(outDir)) {
      Files.walkFileTree(outDir, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(Objects.requireNonNull(file));
          return FileVisitResult.CONTINUE;
        }

      });
    }
  }
}
