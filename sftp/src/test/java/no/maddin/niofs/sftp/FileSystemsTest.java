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

package no.maddin.niofs.sftp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import no.maddin.niofs.commons.AbstractTest;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class FileSystemsTest extends AbstractTest {
  
  @Rule
  public ExpectedException exception = ExpectedException.none();
  
  @Test
  public void getFileSystemURI() throws Exception {
    Assume.assumeNotNull(testprops);
    
    URI uri = createSftpURI("/");
    FileSystem fileSystem = FileSystems.getFileSystem(uri);
    assertThat(fileSystem, is(nullValue()));
  }
  
  @Test
  public void newFileSystem() throws Exception {
    Assume.assumeNotNull(testprops);
    URI uri = createSftpURI("/");
    FileSystem fs = FileSystems.newFileSystem(uri, null);
    
    assertThat(fs, is(notNullValue()));
  }
  
  
  private URI createSftpURI(String path) throws URISyntaxException {
    String host = testprops.getProperty("sftp.host");
    Assume.assumeNotNull(host);
    int port = Integer.parseInt(testprops.getProperty("sftp.port", "-1"));
    return createTestUri("sftp", host, port, path);
  }
}
