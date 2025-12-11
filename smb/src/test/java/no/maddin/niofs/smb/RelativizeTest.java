package no.maddin.niofs.smb;

import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static no.maddin.niofs.testutil.Matchers.hasStringValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
public class RelativizeTest {

    @Container
    public static SambaContainer samba = SambaContainer.runningOr(() -> new SambaContainer("target/test-classes/smb"));

    public static Stream<Arguments> data() throws Exception {
        String sambaAddress = samba.getGuestIpAddress();
        return Stream.of(
            Arguments.of(
                "siblings",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb://" + sambaAddress + "/public/temp/a/"),
                new URI("smb://" + sambaAddress + "/public/temp/b/"),
                "../b"
            ),
            Arguments.of(
                "child",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb://" + sambaAddress + "/public/temp/"),
                new URI("smb://" + sambaAddress + "/public/temp/b/"),
                "b"),
            Arguments.of(
                "cousins",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb://" + sambaAddress + "/public/temp/a/aa/"),
                new URI("smb://" + sambaAddress + "/public/temp/b/ba/"),
                "../../b/ba"
            ),
            Arguments.of(
                "sibling with spaces",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb", sambaAddress, "/public/My%20Documents/Folder%20One/", null),
                new URI("smb", sambaAddress, "/public/My%20Documents/Folder%20Two/", null),
                "../Folder Two"
            ),
            Arguments.of(
                "sibling with username/password and encoded space",
                new URI("smb://smbtest:test@" + sambaAddress + "/public"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/My%20Documents/Folder%20One/"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/My%20Documents/Folder%20Two/"),
                "../Folder Two"
            ),
            Arguments.of(
                "cousins with username/password",
                new URI("smb://smbtest:test@" + sambaAddress + "/public"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/temp/a/aa/"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/temp/b/ba/"),
                "../../b/ba"
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void relativize(String testName, URI shareUri, URI uriA, URI uriB, String expectedPathString) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("USERNAME", "admin");
        env.put("PASSWORD", "admin");
        FileSystem fileSystem = FileSystems.newFileSystem(shareUri, env);
        assertThat(fileSystem, Matchers.is(notNullValue()));

        Path smbA = Paths.get(uriA);

        Path smbB = Paths.get(uriB);

        Path relPath = smbA.relativize(smbB);

        assertThat(relPath, is(notNullValue(Path.class)));

        assertThat(relPath, is(instanceOf(SMBPath.class)));

        assertThat(relPath, hasStringValue(is(expectedPathString)));
    }
}
