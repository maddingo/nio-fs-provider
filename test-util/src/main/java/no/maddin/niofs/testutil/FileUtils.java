package no.maddin.niofs.testutil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileUtils {

    /**
     * Utility class
     */
    private FileUtils() {
    }

    @SuppressWarnings("java:S112")
    public static File classpathFile(Class<?> hostClass, String testDataResource) {
        try {
            return Paths.get(hostClass.getResource(testDataResource).toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "java:S112"})
    public static File writeTestFile(File parent, String fileName) {
        File targetFile = new File(parent, fileName);
        targetFile.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(targetFile)) {
            fw.append("test test, delete file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return targetFile;
    }

    public static List<String> createFilesInDir(File rootFolder, String listingDir, int count) {
        return IntStream.range(1, count + 1)
            .mapToObj(i -> String.format("%s%stestfile-%02d.txt", listingDir, File.separator, i))
            .map(s -> FileUtils.writeTestFile(rootFolder, s))
            .map(File::getName)
            .collect(Collectors.toList());
    }


}
