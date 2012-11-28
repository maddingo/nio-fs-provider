package no.uis.nio.sftp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import no.uis.nio.commons.AbstractTest;

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
    int port = Integer.parseInt(testprops.getProperty("sftp.port"));
    return createTestUri("sftp", host, port, path);
  }
}
