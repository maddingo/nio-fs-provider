package no.maddin.niofs.smb;

import no.maddin.niofs.commons.AbstractTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RelativizeTest extends AbstractTest {

    public static Stream<Arguments> data() throws Exception {
        List<Object[]> data = new ArrayList<>();

        return Stream.of(
            Arguments.of(
                "siblings",
                new URI("smb://localhost/public/temp/a/"),
                new URI("smb://localhost/public/temp/b/"),
                "..\\b\\",
                2
            ),
            Arguments.of(
                "child",
                new URI("smb://localhost/public/temp/"),
                new URI("smb://localhost/public/temp/b/"),
                "b\\",
                1
            ),
            Arguments.of(
                "cousins",
                new URI("smb://localhost/public/temp/a/aa/"),
                new URI("smb://localhost/public/temp/b/ba/"),
                "..\\..\\b\\ba\\",
                3 // successive parent directories are treated as one part
            ),
            Arguments.of(
                "sibling with spaces",
                new URI("smb", "localhost", "/public/My Documents/Folder One/", null),
                new URI("smb", "localhost", "/public/My Documents/Folder Two/", null),
                "..\\Folder Two\\",
                2
            ),
            Arguments.of(
                "sibling with username/password and encoded space",
                new URI("smb://smbtest:test@localhost/public/My%20Documents/Folder%20One/"),
                new URI("smb://smbtest:test@localhost/public/My%20Documents/Folder%20Two/"),
                "..\\Folder Two\\",
                2
            ),
            Arguments.of(
                "cousins with username/password",
                new URI("smb://smbtest:test@localhost/public/temp/a/aa/"),
                new URI("smb://smbtest:test@localhost/public/temp/b/ba/"),
                "..\\..\\b\\ba\\",
                3 // successive parent directories are treated as one part
            )
        );

    }

    @ParameterizedTest
    @MethodSource("data")
    public void relativize(String testName, URI uriA, URI uriB, String expectedPathString, int expectedParts) throws Exception {
        Path smbA = Paths.get(uriA);

        Path smbB = Paths.get(uriB);

        Path relPath = smbA.relativize(smbB);

        assertThat(relPath, is(notNullValue(Path.class)));

        assertThat(relPath, is(instanceOf(SMBBasePath.class)));

        assertThat(relPath.toString(), is(expectedPathString));

        int count = 0;
        for (Path path : relPath) {
            assertThat(path, is(instanceOf(SMBBasePath.class)));
            assertTrue(path.toString().endsWith("\\"));
            count++;
        }
        assertThat(count, is(expectedParts));
    }
}
