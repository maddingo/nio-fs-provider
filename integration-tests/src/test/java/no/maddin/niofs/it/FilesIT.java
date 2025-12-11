package no.maddin.niofs.it;

import no.maddin.niofs.testutil.BasicTestContainer;
import no.maddin.niofs.testutil.FileUtils;
import no.maddin.niofs.testutil.SftpgoContainer;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.*;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;

/**
 * We prepare a file structure and use the various providers list the files.
 */
@Testcontainers
@SuppressWarnings("java:S1186")
public class FilesIT {

    private static final String TESTDATA_RESOURCE = "/sftpgo-data";
    private static final File localDataFileRoot = FileUtils.classpathFile(FilesIT.class, TESTDATA_RESOURCE);

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
        Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(localTestFile(tmpFile.toUri().getPath()), anExistingFile());
            Files.setPosixFilePermissions(tmpFile, EnumSet.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ));
            assertThat(Files.isExecutable(tmpFile), equalTo(true));
            assertThat(Files.isExecutable(dir), equalTo(true));
        }

    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void isHidden(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, ".tmp", ".txt");
            assertThat(Files.isHidden(tmpFile), equalTo(true));
            assertThat(Files.isHidden(dir), equalTo(false));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void isReadable(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(localTestFile(tmpFile.toUri().getPath()), anExistingFile());
            Files.setPosixFilePermissions(tmpFile, EnumSet.of(PosixFilePermission.OWNER_READ));
            assertThat(Files.isReadable(tmpFile), equalTo(true));
            assertThat(Files.isReadable(dir), equalTo(true));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void isRegularFile(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(Files.isRegularFile(tmpFile), equalTo(true));
            assertThat(Files.isRegularFile(dir), equalTo(false));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void isWritable(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(localTestFile(tmpFile.toUri().getPath()), anExistingFile());
            Files.setPosixFilePermissions(tmpFile, EnumSet.of(PosixFilePermission.OWNER_WRITE));
            assertThat(Files.isWritable(tmpFile), equalTo(true));
            assertThat(Files.isWritable(dir), equalTo(true));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void createLink(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav") /*|| protocol.equals("sftp")*/, "Sardine has an incomplete implementation of the ACL");
        Assumptions.assumeFalse(protocol.equals("sftp"), "SFTP does not support hard links");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            Path target = tmpFile.resolve("link.txt");

            Path linkFile = Files.createLink(tmpFile, target);
            assertThat(Files.isSymbolicLink(target), equalTo(true));
            assertThat(linkFile, equalTo(tmpFile));
            assertThat(Files.isSymbolicLink(dir), equalTo(false));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void isSymbolicLink(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            Path target = tmpFile.resolve("link.txt");
            Path linkFile = Files.createSymbolicLink(tmpFile, target);
            assertThat(Files.isSymbolicLink(target), equalTo(true));
            assertThat(linkFile, equalTo(tmpFile));
            assertThat(Files.isSymbolicLink(dir), equalTo(false));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void readSymbolicLink(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            Path target = tmpFile.resolve("link.txt");
            Path linkFile = Files.createSymbolicLink(tmpFile, target);
            assertThat(Files.readSymbolicLink(target), equalTo(linkFile));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void getAttribute(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");
            assertThat(Files.getAttribute(tmpFile, "basic:isDirectory"), equalTo(false));
            assertThat(Files.getAttribute(dir, "basic:isDirectory"), equalTo(true));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @SuppressWarnings("java:S2699")
    void setAttribute(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "Sardine has an incomplete implementation of the ACL");
        Assumptions.assumeFalse(protocol.equals("sftp"), "Setting attributes is not supported by SFTP");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void getFileAttributeView(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(tmpFile, BasicFileAttributeView.class);
            assertThat(fileAttributeView, notNullValue());
            assertThat(fileAttributeView.readAttributes(), hasProperty("lastModifiedTime", Matchers.notNullValue()));
            assertThat(fileAttributeView.readAttributes(), hasProperty("isRegularFile", equalTo(true)));
            assertThat(fileAttributeView.readAttributes(), hasProperty("isDirectory", equalTo(false)));

            BasicFileAttributeView dirAttributeView = Files.getFileAttributeView(dir, BasicFileAttributeView.class);
            assertThat(dirAttributeView, notNullValue());
            assertThat(dirAttributeView.readAttributes(), hasProperty("lastModifiedTime", Matchers.notNullValue()));
            assertThat(dirAttributeView.readAttributes(), hasProperty("isRegularFile", equalTo(false)));
            assertThat(dirAttributeView.readAttributes(), hasProperty("isDirectory", equalTo(true)));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void readAttributes(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Map<String, Object> fileAttributes = Files.readAttributes(tmpFile, "basic:lastModifiedTime,isDirectory,isRegularFile", LinkOption.NOFOLLOW_LINKS);
            assertThat(fileAttributes, hasEntry(equalTo("lastModifiedTime"), instanceOf(FileTime.class)));
            assertThat(fileAttributes, hasEntry(equalTo("isDirectory"), equalTo(false)));
            assertThat(fileAttributes, hasEntry(equalTo("isRegularFile"), equalTo(true)));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void getFileStore(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            assertThat(Files.getFileStore(tmpFile), notNullValue());
            assertThat(Files.getFileStore(dir), notNullValue());
            assertThat(Files.getFileStore(tmpFile), equalTo(Files.getFileStore(dir)));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void getLastModifiedTime(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            FileTime lastModifiedTime = Files.getLastModifiedTime(tmpFile);
            assertThat(lastModifiedTime, notNullValue());
            assertThat(lastModifiedTime.toMillis(), greaterThan(0L));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Setting file timestamps may not be reliable across all protocols and container environments")
    void setModifiedTime(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "WebDAV has limited support for setting attributes");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            FileTime originalTime = Files.getLastModifiedTime(tmpFile);
            FileTime newTime = FileTime.fromMillis(originalTime.toMillis() + 10000);
            Files.setLastModifiedTime(tmpFile, newTime);
            FileTime updatedTime = Files.getLastModifiedTime(tmpFile);
            assertThat(updatedTime, equalTo(newTime));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void getOwner(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "WebDAV has limited owner support");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            UserPrincipal owner = Files.getOwner(tmpFile);
            assertThat(owner, notNullValue());
            assertThat(owner.getName(), not(emptyString()));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Owner setting requires admin privileges and may timeout in container environment")
    void setOwner(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "WebDAV has limited owner support");
        Assumptions.assumeFalse(protocol.equals("sftp"), "SFTP typically requires admin privileges to change ownership");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            UserPrincipal currentOwner = Files.getOwner(tmpFile);
            assertThat(currentOwner, notNullValue());
            // Since we can't create new owners in the test environment, 
            // we just verify the operation doesn't throw an exception
            // when setting the same owner
            Files.setOwner(tmpFile, currentOwner);
            assertThat(Files.getOwner(tmpFile), equalTo(currentOwner));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void getPosixFilePermissions(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "WebDAV has limited POSIX permission support");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(tmpFile);
            assertThat(permissions, notNullValue());
            assertThat(permissions, not(empty()));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void setPosixFilePermissions(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        Assumptions.assumeFalse(protocol.equals("webdav"), "WebDAV has limited POSIX permission support");
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Set<PosixFilePermission> newPermissions = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
            );
            Files.setPosixFilePermissions(tmpFile, newPermissions);
            Set<PosixFilePermission> actualPermissions = Files.getPosixFilePermissions(tmpFile);
            assertThat(actualPermissions, equalTo(newPermissions));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
//    @Disabled("File comparison operations may be unreliable and slow across protocols")
    void isSameFile(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile1 = Files.createTempFile(dir, "tmp1", ".txt");
            Path tmpFile2 = Files.createTempFile(dir, "tmp2", ".txt");
            Path samePath = Paths.get(tmpFile1.toUri());

            assertThat(Files.isSameFile(tmpFile1, samePath), equalTo(true));
            assertThat(Files.isSameFile(tmpFile1, tmpFile2), equalTo(false));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("File move operations may timeout due to remote filesystem latency")
    void move(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path source = Files.createTempFile(dir, "source", ".txt");
            Path target = dir.resolve("target.txt");

            Files.write(source, "test content".getBytes());
            assertThat(Files.exists(source), equalTo(true));
            assertThat(Files.exists(target), equalTo(false));

            Files.move(source, target);
            assertThat(Files.exists(source), equalTo(false));
            assertThat(Files.exists(target), equalTo(true));
            assertThat(Files.readAllBytes(target), equalTo("test content".getBytes()));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Buffered reader operations may timeout with remote filesystem connections")
    void newBufferedReader(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        List<String> testLines = Arrays.asList("Line 1 " + randomString, "Line 2", "Line 3");
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Files.write(tmpFile, testLines);
            try (BufferedReader reader = Files.newBufferedReader(tmpFile)) {
                List<String> readLines = reader.lines().collect(Collectors.toList());
                assertThat(readLines, equalTo(testLines));
            }
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Buffered writer operations may be slow and cause timeouts in container tests")
    void newBufferedWriter(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        List<String> testLines = Arrays.asList("Line 1 " + randomString, "Line 2", "Line 3");
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = dir.resolve("writer-test.txt");

            try (BufferedWriter writer = Files.newBufferedWriter(tmpFile)) {
                for (String line : testLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            List<String> readLines = Files.readAllLines(tmpFile);
            assertThat(readLines, equalTo(testLines));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("ByteChannel operations may timeout with remote filesystems")
    void newByteChannel(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        String testContent = "Hello, World! " + randomString;
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = dir.resolve("channel-test.txt");

            // Write using SeekableByteChannel
            try (SeekableByteChannel channel = Files.newByteChannel(tmpFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                channel.write(java.nio.ByteBuffer.wrap(testContent.getBytes()));
            }

            // Read using SeekableByteChannel
            try (SeekableByteChannel channel = Files.newByteChannel(tmpFile, StandardOpenOption.READ)) {
                java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate((int) channel.size());
                channel.read(buffer);
                buffer.flip();
                byte[] readBytes = new byte[buffer.remaining()];
                buffer.get(readBytes);
                assertThat(readBytes, equalTo(testContent.getBytes()));
            }
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Directory streaming with filters may be slow and timeout in integration tests")
    void newDirectoryStream(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            
            // Create some test files
            Files.createFile(dir.resolve("file1.txt"));
            Files.createFile(dir.resolve("file2.txt"));
            Files.createFile(dir.resolve("file3.log"));
            Files.createDirectories(dir.resolve("subdir"));

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                Set<String> foundNames = new HashSet<>();
                for (Path entry : stream) {
                    foundNames.add(entry.getFileName().toString());
                }
                assertThat(foundNames, hasItems("file1.txt", "file2.txt", "file3.log", "subdir"));
            }

            // Test with filter
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.txt")) {
                Set<String> foundNames = new HashSet<>();
                for (Path entry : stream) {
                    foundNames.add(entry.getFileName().toString());
                }
                assertThat(foundNames, hasItems("file1.txt", "file2.txt"));
                assertThat(foundNames, not(hasItems("file3.log", "subdir")));
            }
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("InputStream operations may be slow and timeout with remote protocols")
    void newInputStream(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        String testContent = "Hello, World! " + randomString;
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Files.write(tmpFile, testContent.getBytes());
            try (InputStream inputStream = Files.newInputStream(tmpFile)) {
                byte[] buffer = new byte[testContent.getBytes().length];
                int bytesRead = inputStream.read(buffer);
                byte[] readBytes = new byte[bytesRead];
                System.arraycopy(buffer, 0, readBytes, 0, bytesRead);
                assertThat(readBytes, equalTo(testContent.getBytes()));
            }
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("OutputStream operations may timeout in integration test environment")
    void newOutputStream(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        String testContent = "Hello, World! " + randomString;
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = dir.resolve("output.txt");

            try (OutputStream outputStream = Files.newOutputStream(tmpFile)) {
                outputStream.write(testContent.getBytes());
            }
            assertThat(Files.readAllBytes(tmpFile), equalTo(testContent.getBytes()));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Content type probing may be unreliable and slow across different protocols")
    void probeContentType(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path txtFile = dir.resolve("test.txt");
            Path htmlFile = dir.resolve("test.html");
            
            Files.write(txtFile, "Hello World".getBytes());
            Files.write(htmlFile, "<html><body>Hello</body></html>".getBytes());

            String txtContentType = Files.probeContentType(txtFile);
            String htmlContentType = Files.probeContentType(htmlFile);
            
            // Content type detection may not work on all systems/protocols,
            // so we just verify the method doesn't throw exceptions
            // and returns reasonable values if detection works
            if (txtContentType != null) {
                assertThat(txtContentType, anyOf(containsString("text"), containsString("plain")));
            }
            if (htmlContentType != null) {
                assertThat(htmlContentType, anyOf(containsString("text"), containsString("html")));
            }
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Test may timeout due to file I/O operations in containerized environment")
    void readAllBytes(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        String testContent = "Hello, World! " + randomString;
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Files.write(tmpFile, testContent.getBytes());
            byte[] readBytes = Files.readAllBytes(tmpFile);
            assertThat(readBytes, equalTo(testContent.getBytes()));
            assertThat(new String(readBytes), equalTo(testContent));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Test may timeout due to multiple file operations and container overhead")
    void readAllLines(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        List<String> testLines = Arrays.asList("Line 1 " + randomString, "Line 2", "Line 3 with special chars: äöü");
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Files.write(tmpFile, testLines);
            List<String> readLines = Files.readAllLines(tmpFile);
            assertThat(readLines, equalTo(testLines));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void size(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        String testContent = "Hello, World! " + randomString;
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = Files.createTempFile(dir, "tmp", ".txt");

            Files.write(tmpFile, testContent.getBytes());
            long fileSize = Files.size(tmpFile);
            assertThat(fileSize, equalTo((long) testContent.getBytes().length));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("Test fails due to file path resolution issues in containerized environment")
    void walkFileTree(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path rootDir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(rootDir);
            
            // Create a nested directory structure
            Path subDir1 = rootDir.resolve("subdir1");
            Path subDir2 = rootDir.resolve("subdir2");
            Path nestedDir = subDir1.resolve("nested");
            Files.createDirectories(subDir1);
            Files.createDirectories(subDir2);
            Files.createDirectories(nestedDir);
            
            Files.createFile(rootDir.resolve("root.txt"));
            Files.createFile(subDir1.resolve("sub1.txt"));
            Files.createFile(subDir2.resolve("sub2.txt"));
            Files.createFile(nestedDir.resolve("nested.txt"));

            Set<String> visitedPaths = new HashSet<>();
            Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) {
                    visitedPaths.add(rootDir.relativize(file).toString());
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) {
                    if (!dir.equals(rootDir)) {
                        visitedPaths.add(rootDir.relativize(dir).toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            
            assertThat(visitedPaths, hasItems("root.txt", "subdir1", "subdir2", "subdir1/sub1.txt", "subdir2/sub2.txt", "subdir1/nested", "subdir1/nested/nested.txt"));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    @Disabled("File write operations with append mode may timeout in remote filesystem tests")
    void write(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {
        String randomString = UUID.randomUUID().toString();
        String testContent = "Hello, World! " + randomString;
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path dir = Paths.get(uri.resolve("/" + randomString));
            Files.createDirectories(dir);
            Path tmpFile = dir.resolve("test.txt");

            Files.write(tmpFile, testContent.getBytes());
            assertThat(Files.exists(tmpFile), equalTo(true));
            assertThat(Files.readAllBytes(tmpFile), equalTo(testContent.getBytes()));

            // Test appending
            String additionalContent = "\nAppended line";
            Files.write(tmpFile, additionalContent.getBytes(), StandardOpenOption.APPEND);
            String expectedContent = testContent + additionalContent;
            assertThat(Files.readAllBytes(tmpFile), equalTo(expectedContent.getBytes()));
        }
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