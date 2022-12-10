package no.maddin.niofs.webdav;
//CHECKSTYLE:OFF

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * These are the tests that don't require a running server.
 */
public class WebdavPathTest {

    private int webdavPort = -1;

    @Test
    public void newFileSystemWebdav() throws Exception {
        URI uri = new URI("webdav", "user:password","localhost", webdavPort, "/", null, null);

        FileSystem fs = FileSystems.newFileSystem(uri, null);

        assertThat(fs, is(notNullValue()));
    }

    @Test
    public void newFileSystemWebdavs() throws Exception {
        URI uri = new URI("webdavs", "user:password","localhost", webdavPort, "/", null, null);

        FileSystem fs = FileSystems.newFileSystem(uri, null);

        assertThat(fs, is(notNullValue()));
    }

    @Test
    public void getURI() throws Exception {
        URI uri = new URI("webdav", "user:password","localhost", webdavPort, "/", null, null);

        Path path = Paths.get(uri);

        assertThat(path, is(notNullValue()));
    }

    @Test
    public void normalize() throws Exception {
        String dottedPath = "/webdav/../test/something";

        URI uri = new URI("webdav", "username:password", "anyhost", webdavPort, dottedPath, null, null);

        Path path = Paths.get(uri);
        Path result = path.normalize();

        assertThat(result, is(instanceOf(WebdavPath.class)));

        String resultUri = result.toUri().toString();
        assertThat(resultUri, not(containsString("..")));
        assertThat(result.isAbsolute(), is(true));
    }
}
