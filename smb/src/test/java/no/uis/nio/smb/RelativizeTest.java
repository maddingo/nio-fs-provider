package no.uis.nio.smb;

import no.uis.nio.commons.AbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class RelativizeTest extends AbstractTest {

    private final URI uriA;
    private final URI uriB;
    private final String expectedPathString;
    private final int expectedParts;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() throws Exception {
        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] {
                "siblings",
                new URI("smb://localhost/public/temp/a/"),
                new URI("smb://localhost/public/temp/b/"),
                "..\\b\\",
                2
        });

        data.add(new Object[] {
                "child",
                new URI("smb://localhost/public/temp/"),
                new URI("smb://localhost/public/temp/b/"),
                "b\\",
                1
        });

        data.add(new Object[] {
                "cousins",
                new URI("smb://localhost/public/temp/a/aa/"),
                new URI("smb://localhost/public/temp/b/ba/"),
                "..\\..\\b\\ba\\",
                3 // successive parent directories are treated as one part
        });

        data.add(new Object[] {
                "sibling with spaces",
                new URI("smb", "localhost", "/public/My Documents/Folder One/", null),
                new URI("smb", "localhost", "/public/My Documents/Folder Two/", null),
                "..\\Folder Two\\",
                2
        });

        data.add(new Object[] {
                "sibling with username/password and encoded space",
                new URI("smb://smbtest:test@localhost/public/My%20Documents/Folder%20One/"),
                new URI("smb://smbtest:test@localhost/public/My%20Documents/Folder%20Two/"),
                "..\\Folder Two\\",
                2
        });

        data.add(new Object[] {
                "cousins with username/password",
                new URI("smb://smbtest:test@localhost/public/temp/a/aa/"),
                new URI("smb://smbtest:test@localhost/public/temp/b/ba/"),
                "..\\..\\b\\ba\\",
                3 // successive parent directories are treated as one part
        });

        return data;
    }

    public RelativizeTest(String testName, URI uria, URI urib, String expectedPathString, int expectedParts) {

        this.uriA = uria;
        this.uriB = urib;
        this.expectedPathString = expectedPathString;
        this.expectedParts = expectedParts;
    }

    @Test
    public void relative() throws Exception {
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
