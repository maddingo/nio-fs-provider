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

import jcifs.smb.SmbFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class CifsTest extends AbstractSmbTest {

    private String[] childrenUrls;
    private String parentUrl;
    private String[] childrenUris;
    private String parentUri;

    @Parameterized.Parameters(name ="{index} {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] {
                "smb://127.0.0.1/public/My Documents/",
                new String[] {"smb://127.0.0.1/public/My Documents/Folder One/", "smb://127.0.0.1/public/My Documents/Folder Two/"}
        });
        return data;
    }

    public CifsTest(String parentUrl, String[] childrenUrls) throws MalformedURLException, URISyntaxException {
        this.parentUrl = parentUrl;
        this.childrenUrls = childrenUrls;
        URL parent = new URL(parentUrl);
        this.parentUri = new URI(parent.getProtocol(), parent.getAuthority(), parent.getPath(), null).toString();
        this.childrenUris = new String[childrenUrls.length];
        for (int i = 0; i < childrenUrls.length; i++) {
            URL child = new URL(childrenUrls[i]);
            this.childrenUris[i] = new URI(child.getProtocol(), child.getAuthority(), child.getPath(), null).toString();
        }
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void directoryStreamIterator() throws Exception {

        // ignore the test if the expected test file system does not exist
        for (String child : childrenUrls) {
            SmbFile smbf = new SmbFile(child);
            assumeThat("Test file " + smbf.toString() + " exists", smbf.canRead(), is(true));
        }

        Path remotePath = Paths.get(new URI(parentUri));
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

        assertThat(fileNames, hasItems(childrenUris));
    }
}
