package no.maddin.niofs.smb;

import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class RelativizeTest {

    @Container
    public static SambaContainer samba = new SambaContainer("target/test-classes/smb");

    public static Stream<Arguments> data() throws Exception {
        String sambaAddress = samba.getGuestIpAddress();

        return Stream.of(
            Arguments.of(
                "siblings",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb://" + sambaAddress + "/public/temp/a/"),
                new URI("smb://" + sambaAddress + "/public/temp/b/"),
                "..\\b\\",
                2
            ),
            Arguments.of(
                "child",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb://" + sambaAddress + "/public/temp/"),
                new URI("smb://" + sambaAddress + "/public/temp/b/"),
                "b\\",
                1
            ),
            Arguments.of(
                "cousins",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb://" + sambaAddress + "/public/temp/a/aa/"),
                new URI("smb://" + sambaAddress + "/public/temp/b/ba/"),
                "..\\..\\b\\ba\\",
                3 // successive parent directories are treated as one part
            ),
            Arguments.of(
                "sibling with spaces",
                new URI("smb://" + sambaAddress + "/public"),
                new URI("smb", "" + sambaAddress + "", "/public/My Documents/Folder One/", null),
                new URI("smb", "" + sambaAddress + "", "/public/My Documents/Folder Two/", null),
                "..\\Folder Two\\",
                2
            ),
            Arguments.of(
                "sibling with username/password and encoded space",
                new URI("smb://smbtest:test@" + sambaAddress + "/public"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/My%20Documents/Folder%20One/"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/My%20Documents/Folder%20Two/"),
                "..\\Folder Two\\",
                2
            ),
            Arguments.of(
                "cousins with username/password",
                new URI("smb://smbtest:test@" + sambaAddress + "/public"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/temp/a/aa/"),
                new URI("smb://smbtest:test@" + sambaAddress + "/public/temp/b/ba/"),
                "..\\..\\b\\ba\\",
                3 // successive parent directories are treated as one part
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void relativize(String testName, URI shareUri, URI uriA, URI uriB, String expectedPathString, int expectedParts) throws Exception {
        FileSystem fileSystem = FileSystems.newFileSystem(shareUri, Map.of("USERNAME", "admin", "PASSWORD", "admin"));
        assertThat(fileSystem, Matchers.is(notNullValue()));

        Path smbA = Paths.get(uriA);

        Path smbB = Paths.get(uriB);

        Path relPath = smbA.relativize(smbB);

        assertThat(relPath, is(notNullValue(Path.class)));

        assertThat(relPath, is(instanceOf(SMBPath.class)));

        assertThat(relPath.toString(), is(expectedPathString));

        int count = 0;
        for (Path path : relPath) {
            assertThat(path, is(instanceOf(SMBPath.class)));
            assertTrue(path.toString().endsWith("\\"));
            count++;
        }
        assertThat(count, is(expectedParts));
    }
}
