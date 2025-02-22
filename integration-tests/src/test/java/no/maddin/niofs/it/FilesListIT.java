package no.maddin.niofs.it;

import no.maddin.niofs.testutil.BasicTestContainer;
import no.maddin.niofs.testutil.FileUtils;
import no.maddin.niofs.testutil.SftpgoContainer;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;

/**
 * We prepare a file structure and use the various providers list the files.
 */
@Testcontainers
public class FilesListIT {

    private static final String TESTDATA_RESOURCE = "/sftpgo-data";
    private static final File localDataFileRoot = FileUtils.classpathFile(FilesListIT.class, TESTDATA_RESOURCE);

    public static Stream<Arguments> data() {
        // anonymous class
        return Stream.of(
            Arguments.of(
                "sftp",
                (Supplier<BasicTestContainer>) () -> new SftpgoContainer(TESTDATA_RESOURCE)
            ),
            Arguments.of(
                "webdav",
                (Supplier<BasicTestContainer>) () -> new SftpgoContainer(TESTDATA_RESOURCE)
            )
        );
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void listFiles(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {

        String dataSubDir = "/" + UUID.randomUUID();
        SortedSet<String> createdFiles = FileUtils.createFilesInDir(localDataFileRoot, dataSubDir, 10);
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path path = Paths.get(uri.resolve(dataSubDir));
            try (Stream<Path> paths = Files.list(path)) {
                SortedSet<String> foundFiles = paths
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toCollection(TreeSet::new));

                assertThat(foundFiles, equalTo(createdFiles));
            }
        }
    }

    /**
     * Files.copy
     */
    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void copyFiles(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path sourcePath = Paths.get(uri.resolve("/testfile.txt"));
            Path targetPath = Paths.get(uri.resolve("/testfile-" + randomString + ".txt"));
            Files.copy(sourcePath, targetPath);
            assertThat(localTestFile("testfile-" + randomString + ".txt"), anExistingFile());
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void createDirectory(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path path = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectory(path);
            assertThat(localTestFile(randomString), anExistingDirectory());
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void createDirecories(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomDir = UUID.randomUUID().toString();
        String randomSubDir = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path path = Paths.get(uri.resolve("/" + randomDir + "/" + randomSubDir));
            Files.createDirectories(path);
            assertThat(localTestFile(randomDir, randomSubDir), anExistingDirectory());
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void createFile(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path path = Paths.get(uri.resolve("/" + randomString + ".txt"));
            Files.createFile(path);
            assertThat(localTestFile(randomString + ".txt"), anExistingFile());
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void createTempDirectory(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpPath = Files.createTempDirectory(dir, "tmp");
            assertThat(localTestFile(tmpPath.toUri().getPath()), anExistingDirectory());
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void createTempFile(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(localTestFile(tmpFile.toUri().getPath()), anExistingFile());
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void delete(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(localTestFile(tmpFile.toUri().getPath()), anExistingFile());
            Files.delete(tmpFile);
            assertThat(localTestFile(tmpFile.toUri().getPath()), not(anExistingFile()));
        }


    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void deleteIfExists(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            // if the file is a directory and could not otherwise be deleted because the directory is not empty (optional specific exception)
            Assert.assertThrows(DirectoryNotEmptyException.class, () -> Files.deleteIfExists(dir));
            assertThat(localTestFile(tmpFile.toUri().getPath()), anExistingFile());
            Files.delete(tmpFile);
            assertThat(localTestFile(tmpFile.toUri().getPath()), not(anExistingFile()));
            assertThat(Files.deleteIfExists(tmpFile), equalTo(false));
            assertThat(localTestFile(tmpFile.toUri().getPath()), not(anExistingFile()));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void exists(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(Files.exists(tmpFile), equalTo(true));
            Files.delete(tmpFile);
            assertThat(Files.exists(tmpFile), equalTo(false));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void isDirectory(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(Files.isDirectory(tmpFile), equalTo(false));
            assertThat(Files.isDirectory(dir), equalTo(true));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void isExecutable(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        //Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(localTestFile(tmpFile.toUri().getPath()), anExistingFile());
            Files.setPosixFilePermissions(tmpFile, Collections.singleton(PosixFilePermission.OWNER_EXECUTE));
            assertThat(Files.isExecutable(tmpFile), equalTo(false));
            assertThat(Files.isExecutable(dir), equalTo(true));
        }

    }

    void isHidden() {

    }

    void isReadable() {

    }

    void isRegularFile() {

    }

    void isWritable() {

    }

    void createLink() {

    }

    void createSymbolicLink() {

    }

    void isSbolicLink() {

    }

    void isSymbolicLink() {

    }

    void readSymbolicLink() {

    }

    void getAttribute() {
    }

    void setAttribute() {
    }

    void getFileAttributeView() {
    }

    void readAttributes() {
    }

    void getFileStore() {
    }

    void getLasetModifiedTime() {
    }

    void setModifiedTime() {
    }

    void getOwner() {
    }

    void setOwner() {
    }

    void getPosixFilePermissions() {
    }

    void setPosixFilePermissions() {
    }

    void isSameFile() {
    }

    void move() {
    }

    void newBufferedReader() {
    }

    void newBufferedWriter() {
    }

    void newByteChannel() {
    }

    void bewDirectoryStream() {
    }

    void newInputStream() {
    }

    void newOutputStream() {
    }

    void probeContentType() {
    }

    void readAllBytes() {
    }


    void readAllLines() {
    }

    void size() {
    }

    void walkFileTree() {
    }

    void write() {
    }

    private static File localTestFile(String... parts) {
        if (parts == null || parts.length == 0) {
            return localDataFileRoot;
        }
        Deque<String> dparts = new LinkedList<>(Arrays.asList(parts));
        return newFile(localDataFileRoot, dparts);
    }

    private static File newFile(File parent, Deque<String> children) {
        if (children.isEmpty()) {
            return parent;
        }
        File newParent = new File(parent, children.pop());
        return newFile(newParent, children);
    }
}