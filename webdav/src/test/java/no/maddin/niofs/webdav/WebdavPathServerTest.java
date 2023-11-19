package no.maddin.niofs.webdav;

import org.hamcrest.Matchers;
import org.hamcrest.io.FileMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.io.FileMatchers.*;

@Testcontainers
public class WebdavPathServerTest {

    public static final FileAttribute<Set<PosixFilePermission>> FILE_ATTRIBUTE_OWNER_ALL = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
    public static final String TESTDATA_RESOURCE = "/testdata";
    @Container
    public static WebdavContainer webdav = new WebdavContainer(TESTDATA_RESOURCE);

    private static final File rootFolder = classpathFile(TESTDATA_RESOURCE);

    /**
     * Tests {@link Files#createDirectories(Path, FileAttribute[])} with WebDavPath
     */
    @Test
    public void getCreateChildPath() throws Exception {
        URI uri = URI.create(webdav.getWebdavUrl() + "/a/b");
        Path path = Paths.get(uri);
        Path newPath = Files.createDirectories(path);

        assertThat(newPath, is(notNullValue()));
        File fileA = new File(rootFolder.getAbsolutePath(), "a");
        assertThat(fileA, anExistingDirectory());
        File fileB = new File(fileA.getAbsolutePath(), "b");
        assertThat(fileB, anExistingDirectory());
    }

    /**
     * Tests {@link Files#copy(Path, Path, CopyOption...)} with WebdavPath as target.
     */
    @Test
    public void copyFiles() throws Exception {
        File src = File.createTempFile("webdavtest", ".txt", new File("target"));
        String testTest = UUID.randomUUID().toString();
        try (FileWriter fw = new FileWriter(src)) {
            fw.append(testTest);
        }

        URI uriTo = URI.create(webdav.getWebdavUrl() + "/test2/file.txt");
        Path uriToParent = Paths.get(uriTo).getParent();
        Files.createDirectory(uriToParent, FILE_ATTRIBUTE_OWNER_ALL);

        Path pathTo = Paths.get(uriTo);
        Files.copy(src.toPath(), pathTo, StandardCopyOption.REPLACE_EXISTING);

        File fileTxt = new File(new File(rootFolder, "test2"), "file.txt");
        assertThat(fileTxt, FileMatchers.anExistingFile());
        assertThat(Files.readAllLines(fileTxt.toPath()), hasItem(testTest));
    }

    @Test
    public void deleteNonexistingFile() throws Exception {
        String randomFile = String.format("/file-%s.txt", UUID.randomUUID());
        URI uri = URI.create(webdav.getWebdavUrl() + randomFile);

        Path path = Paths.get(uri);
        Assert.assertThrows(NoSuchFileException.class, () -> Files.delete(path));
    }

    @Test
    public void deleteFile() throws Exception {
        File targetFile = writeTestFile("test2" + File.separator + "file.txt");
        assertThat(targetFile, anExistingFile());

        URI uri = URI.create(webdav.getWebdavUrl() + "/test2/file.txt");
        Path path = Paths.get(uri);

        Files.delete(path);

        assertThat(targetFile, not(anExistingFile()));
    }

    @Test
    public void deleteWrongHost() throws Exception {

        URI uri = new URI("webdav", "user:password","non-existing-host", 2022, "/", null, null);
        Path path = Paths.get(uri);
        Assert.assertThrows(UnknownHostException.class, () -> Files.delete(path));
    }

    @Test
    public void readFileAttributes() throws Exception {
        String randomFile = String.format("testfile-%s.txt", UUID.randomUUID());
        File testFile = writeTestFile(randomFile);

        URI uri = URI.create(webdav.getWebdavUrl() + "/" + randomFile);

        BasicFileAttributes attrs = Files.readAttributes(Paths.get(uri), BasicFileAttributes.class);

        assertThat(attrs, is(notNullValue()));
        assertThat(attrs.size(), is(greaterThan(0L)));
        assertThat(attrs.isDirectory(), is(false));
        assertThat(attrs.isRegularFile(), is(true));
        assertThat(attrs.isSymbolicLink(), is(false));
        assertThat(attrs.isOther(), is(false));
        assertThat(attrs.lastAccessTime(), is(notNullValue()));
        assertThat(attrs.lastModifiedTime(), is(notNullValue()));
        //assertThat(attrs.creationTime(), is(notNullValue())); // creation time is not returned for some reason
    }

    /**
     * Tests {@link java.nio.file.spi.FileSystemProvider#newDirectoryStream(Path, DirectoryStream.Filter)}
     * and {@link java.nio.file.spi.FileSystemProvider#readAttributes(Path, String, LinkOption...)}
     * with WebDavPath
     */

    @Test
    public void testDirListing(TestReporter reporter) throws Exception {

        String listingDir = UUID.randomUUID().toString();
        List<String> testfileNames = IntStream.range(1, 10)
            .mapToObj(i -> String.format("%s%stestfile-%02d.txt", listingDir, File.separator, i))
            .map(this::writeTestFile)
            .map(File::getName)
            .collect(Collectors.toList());

        URI uri = URI.create(webdav.getWebdavUrl() + "/" + listingDir);

        try (Stream<Path> paths = Files.list(Paths.get(uri))) {
            List<String> foundFiles = paths
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());

            assertThat(foundFiles, Matchers.equalTo(testfileNames));
        }
    }

    /**
     * Not needed anymore, we assert the result inside the container.
     */
//    @Deprecated
    private static File classpathFile(String testDataResource) {
        try {
            return Paths.get(WebdavPathServerTest.class.getResource(testDataResource).toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    
    private File writeTestFile(String filePath) {
        File targetFile = new File(rootFolder.getAbsolutePath(), filePath);
        targetFile.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(targetFile)) {
            fw.append("test test, delete file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return targetFile;
    }
}
