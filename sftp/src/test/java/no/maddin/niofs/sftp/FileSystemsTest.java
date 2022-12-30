package no.maddin.niofs.sftp;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Tests that don't require a server.
 */
class FileSystemsTest {

    /**
     * These are valid URIs for {@link FileSystems#newFileSystem(URI, Map)} for the smb scheme.
     */
    static Stream<Arguments> uriData() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                URI.create("sftp://172.0.2.3"),
                allOf(
                    instanceOf(SFTPHost.class),
                    hasProperty("port", equalTo(22)),
                    hasProperty("host", equalTo("172.0.2.3")),
                    hasProperty("password", nullValue()),
                    hasProperty("username", nullValue())
                )
            ),
            Arguments.of(
                URI.create("sftp://172.0.2.3:2222"),
                allOf(
                    instanceOf(SFTPHost.class),
                    hasProperty("port", equalTo(2222)),
                    hasProperty("host", equalTo("172.0.2.3")),
                    hasProperty("password", nullValue()),
                    hasProperty("username", nullValue()),
                    hasProperty("serverUri", equalTo(URI.create("sftp://172.0.2.3:2222")))
                )
            ),
            Arguments.of(
                new URI("sftp", "user:info", "host", -1, null, null, null),
                allOf(
                    instanceOf(SFTPHost.class),
                    hasProperty("port", equalTo(22)),
                    hasProperty("host", equalTo("host")),
                    hasProperty("password", equalTo("info")),
                    hasProperty("username", equalTo("user")),
                    hasProperty("serverUri", equalTo(URI.create("sftp://user:info@host:22")))
                )
            )
        );
    }

    /**
     * These are invalid URIs for {@link FileSystems#newFileSystem(URI, Map)} for the smb scheme.
     */
    static Stream<Arguments> invalidFsUriData() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                URI.create("sftp://172.0.2.3/"),
                allOf(
                    instanceOf(IllegalArgumentException.class),
                    hasProperty("message", equalTo("Path should be empty"))
                )
            ),
            Arguments.of(
                URI.create("sftp://172.0.2.3/../path"),
                allOf(
                    instanceOf(IllegalArgumentException.class),
                    hasProperty("message", equalTo("Path should be empty"))
                )
            ),
            Arguments.of(
                new URI("sftp", "user:info", "host", -1, null, null, "fragment"),
                allOf(
                    instanceOf(IllegalArgumentException.class),
                    hasProperty("message", equalTo("Fragment should be empty"))
                )
            ),
            Arguments.of(
                new URI("sftp", "user:info", "host", -1, null, "query-key=query-value", null),
                allOf(
                    instanceOf(IllegalArgumentException.class),
                    hasProperty("message", equalTo("Query should be empty"))
                )
            )
        );
    }

    static Stream<Arguments> validGet() {
        return Stream.of(
            Arguments.of(
                URI.create("sftp://localhost"),
                URI.create("sftp://localhost")
            ),
            Arguments.of(
                URI.create("sftp://localhost"),
                URI.create("sftp://localhost:22")
            ),
            Arguments.of(
                URI.create("sftp://localhost:22"),
                URI.create("sftp://localhost")
            ),
            Arguments.of(
                URI.create("sftp://localhost:22"),
                URI.create("sftp://localhost:22")
            ),
            Arguments.of(
                URI.create("sftp://user:pw@localhost"),
                URI.create("sftp://user:pw@localhost:22")
            )
        );
    }

    public static Stream<Arguments> differentUserInfoData() {
        return Stream.of(
            Arguments.of(
                URI.create("sftp://user1:pw1@172.0.3.2:22"),
                URI.create("sftp://user2:pw2@172.0.3.2:22")
            )
        );
    }


    /**
     * Valid calls for {@link FileSystems#newFileSystem(URI, Map)} for the smb scheme.
     */
    @ParameterizedTest
    @MethodSource("uriData")
    void fileSystemFromURI(URI uri, Matcher<FileSystem> expectedResult) throws IOException {
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
            assertThat(fileSystem, expectedResult);
        }
    }

    /**
     * Invalid calls for {@link FileSystems#newFileSystem(URI, Map)} for the smb scheme.
     */
    @ParameterizedTest
    @MethodSource("invalidFsUriData")
    void failingSystemFromURI(URI uri, Matcher<Throwable> expectedResult) {
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
            fail("getFileSystem from " + uri + " should fail");
        } catch (Throwable tr) {
            assertThat(tr, expectedResult);
        }
    }

    @ParameterizedTest
    @MethodSource("validGet")
    void validGetFileSystem(URI uriNew, URI uriGet) throws IOException {
        FileSystem fsCheck;
        try (FileSystem fsNew = FileSystems.newFileSystem(uriNew, Map.of())) {
            fsCheck = fsNew;
            assertThat(fsNew, hasProperty("open", equalTo(true)));
            FileSystem fsGet = FileSystems.getFileSystem(uriGet);
            assertThat(fsGet, notNullValue());
            assertThat(fsNew, equalTo(fsGet));
        }
        assertThat(fsCheck, hasProperty("open", equalTo(false)));
    }

    @ParameterizedTest
    @MethodSource("validGet")
    void validGetUriOnClosedFileSystem(URI uriNew, URI uriGet) {
        FileSystem fsCheck = null;
        try (FileSystem fsNew = FileSystems.newFileSystem(uriNew, Map.of())) {
            fsCheck = fsNew;
            assertThat(fsNew, hasProperty("open", equalTo(true)));
            fsNew.close();
            FileSystem fsGet = FileSystems.getFileSystem(uriGet);
            assertThat("getFileSystem should fail on a closed FileSystem", fsGet, nullValue());
        } catch (Exception ex) {
            assertThat(ex, instanceOf(FileSystemNotFoundException.class));
        }
        assertThat(fsCheck, hasProperty("open", equalTo(false)));
    }

    @ParameterizedTest
    @MethodSource("differentUserInfoData")
    void getFsWithDifferentUserInfo(URI uriNew, URI uriGet) {
        FileSystem fsCheck = null;
        try (FileSystem fsNew = FileSystems.newFileSystem(uriNew, Map.of())) {
            fsCheck = fsNew;
            assertThat(fsNew, hasProperty("open", equalTo(true)));
            FileSystem fsGet = FileSystems.getFileSystem(uriGet);
            assertThat("getFileSystem should fail with different User Info", fsGet, nullValue());
        } catch (Exception ex) {
            assertThat(ex, instanceOf(FileSystemNotFoundException.class));
        }
        assertThat(fsCheck, hasProperty("open", equalTo(false)));
    }
}
