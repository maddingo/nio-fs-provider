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

package no.uis.nio.smb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

import jcifs.Config;
import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.util.LogStream;

import no.uis.nio.commons.AbstractTest;
import no.uis.nio.commons.CatalogCreatorMock;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CifsTest extends AbstractTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void init() throws Exception {
        InputStream config = CifsTest.class.getResourceAsStream("/jcifs-test-config.properties");
        if (config != null) {
            Config.load(config);
            int loglevel = Config.getInt("jcifs.util.loglevel", Integer.MIN_VALUE);
            if (loglevel != Integer.MIN_VALUE) {
                LogStream.setLevel(loglevel);
            }
        }
        Config.registerSmbURLHandler();
    }

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
    public void relativeIterator() throws Exception {
        URI uriA = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/a/a/a/");
        Path smbA = Paths.get(uriA);

        URI uriB = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/b/b/");
        Path smbB = Paths.get(uriB);

        Path relPath = smbA.relativize(smbB);

        assertThat(relPath, is(notNullValue(Path.class)));

        assertThat(relPath, is(instanceOf(SMBBasePath.class)));

        assertThat(relPath.toString(), is("..\\..\\..\\b\\b\\"));

        int count = 0;
        for (Path path : relPath) {
            assertThat(path, is(instanceOf(SMBBasePath.class)));
            assertTrue(path.toString().endsWith("\\"));
            count++;
        }
        assertThat(count, is(3));
    }

    @Test
    public void testUriWithSpace() throws Exception {
        URI uriSpace = createTestUri("smb", "localhost", -1, "/c$/Program Files/");
        
        Path smb = Paths.get(uriSpace);
        for (Path p: smb) {
            assertNotNull(p);
        }
    }

    @Test
    public void testSMBDirectoryStreamIteratorWithSpace() throws Exception {
        exception.expect(URISyntaxException.class);

        new SMBPath(new SMBFileSystemProvider(), new URI("smb://localhost/c$/Program Files/"));
    }

    private static Path createOutPath(Path base, String type, int year,
                                      String fsSemester, String language) {

        return base.
                resolve(type+"/").
                resolve(String.valueOf(year)+"/").
                resolve(fsSemester+"/").
                resolve(language+"/");
    }

    private static Matcher<String> endsWith(final String string) {
        return new BaseMatcher<String> () {

            @Override
            public boolean matches(Object value) {
                return (value instanceof String && ((String)value).endsWith(string));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a string that ends with ").appendValue(string);
            }
        };
    }

    @Test
    public void testSamba() throws Exception {

        SmbFile smbf = new SmbFile("smb://127.0.0.1/public/Untitled Folder/");
        assumeThat("Test file " + smbf.toString() + " exists", smbf.canRead(), is(true));
        Path remotePath = Paths.get(new URI("smb://127.0.0.1/public/"));

        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(remotePath, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return !Files.isSymbolicLink(entry);
            }
        })) {
            for (Path path : directoryStream) {
                fileNames.add(path.toString());
            }
        }
        assertThat(fileNames, hasItem("smb://127.0.0.1/public/Untitled%20Folder"));
    }
}
