import no.maddin.niofs.testutil.BasicTestContainer;
import no.maddin.niofs.testutil.FileUtils;
import no.maddin.niofs.testutil.SftpgoContainer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.io.FileMatchers.anExistingFile;

/**
 * We prepare a file structure and use the various providers list the files.
 */
@Testcontainers
public class FilesListTest {

    private static final String TESTDATA_RESOURCE = "/sftpgo-data";
    private static final File localDataFileRoot = FileUtils.classpathFile(FilesListTest.class, TESTDATA_RESOURCE);

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
//    @ParameterizedTest(name = "{index} {0}")
//    @MethodSource("data")
    void copyFiles(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path sourcePath = Paths.get(uri.resolve("testfile.txt"));
            Path targetPath = Paths.get(uri.resolve("testfile-" + randomString + ".txt"));
            Files.copy(sourcePath, targetPath);
            assertThat(new File(localDataFileRoot, "testfile-" + randomString + ".txt"), anExistingFile());
        }
    }

    void createDirectory() {
    }

    void createDirecories() {

    }

    void createFile() {

    }

    void createTempDirectory() {

    }

    void createTempFile() {

    }

    void delete() {

    }

    void deleteIfExists() {

    }

    void exists() {

    }

    void isDirectory() {

    }

    void isExecutable() {

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
}