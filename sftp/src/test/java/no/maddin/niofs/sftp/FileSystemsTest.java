package no.maddin.niofs.sftp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Tests that don't require a server.
 */
public class FileSystemsTest {

    private URI uri;

    @BeforeEach
    public void createUri() throws Exception {
        uri = new URI("sftp", null, "localhost", -1, "/", null, null);
    }

    @Test
    public void getFileSystemURI() throws Exception {
        FileSystem fileSystem = FileSystems.getFileSystem(uri);
        assertThat(fileSystem, is(notNullValue()));
    }

    @Test
    public void newFileSystem() throws Exception {
        FileSystem fs = FileSystems.newFileSystem(uri, null);

        assertThat(fs, is(notNullValue()));
    }

    @Test
    public void getUri() throws Exception {
        Path path = Paths.get(uri);

        assertThat(path, is(instanceOf(SFTPPath.class)));
    }

    @Test
    public void normalize() throws Exception {

        uri = new URI("sftp", null, "localhost", -1, "/parent/../afterPArent/file.txt", null, null);
        Path path = Paths.get(uri);

        Path normal = path.normalize();

        assertThat(normal, is(notNullValue()));
        assertThat(normal.toUri().toString(), not(containsString("..")));
    }
}
