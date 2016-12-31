/*
 Copyright 2012-2013 University of Stavanger, Norway

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package no.maddin.niofs.webdav;
//CHECKSTYLE:OFF

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import no.maddin.niofs.commons.AbstractTest;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * These are the tests that don't require a running server.
 */
public class WebdavPathTest {

    private int webdavPort = -1;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void nomalizeTest() throws Exception {
        String relPath = "/webdav/../test/something";

        // server uri, the scheme is ignored
        URI serverUri = new URI(null, "username:password", "anyhost", webdavPort, null, null, null);

        Path path = new WebdavPath(new WebdavFileSystem(new WebdavFileSystemProvider(), serverUri), relPath);
        Path result = path.normalize();

        assertThat(result, is(instanceOf(WebdavPath.class)));
        assertThat(result.isAbsolute(), is(true));
    }

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
}
