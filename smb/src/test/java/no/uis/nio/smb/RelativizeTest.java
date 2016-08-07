package no.uis.nio.smb;

import no.uis.nio.commons.AbstractTest;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RelativizeTest extends AbstractTest {
    @Test
    public void relativizeSibling() throws Exception {
        Path smbA = Paths.get(new URI("smb://localhost/public/temp/a/"));

        Path smbB = Paths.get(new URI("smb://localhost/public/temp/b/"));

        Path relPath = smbA.relativize(smbB);

        assertThat(relPath, is(notNullValue(Path.class)));

        assertThat(relPath, is(instanceOf(SMBBasePath.class)));

        assertThat(relPath.toString(), is("..\\b\\"));
    }

    @Test
    public void relativizeChild() throws Exception {
        Path smbA = Paths.get(new URI("smb://localhost/public/temp/"));

        Path smbB = Paths.get(new URI("smb://localhost/public/temp/b/"));

        Path relPath = smbA.relativize(smbB);

        assertThat(relPath, is(notNullValue(Path.class)));

        assertThat(relPath, is(instanceOf(SMBBasePath.class)));

        assertThat(relPath.toString(), is("b\\"));
    }

    @Test
    public void relativeCousin() throws Exception {
        Path smbA = Paths.get(new URI("smb://localhost/public/temp/a/aa/"));

        Path smbB = Paths.get(new URI("smb://localhost/public/temp/b/ba/"));

        Path relPath = smbA.relativize(smbB);

        assertThat(relPath, is(notNullValue(Path.class)));

        assertThat(relPath, is(instanceOf(SMBBasePath.class)));

        assertThat(relPath.toString(), is("..\\..\\b\\ba\\"));

        int count = 0;
        for (Path path : relPath) {
            assertThat(path, is(instanceOf(SMBBasePath.class)));
            assertTrue(path.toString().endsWith("\\"));
            count++;
        }
        assertThat(count, is(3));
    }
}
