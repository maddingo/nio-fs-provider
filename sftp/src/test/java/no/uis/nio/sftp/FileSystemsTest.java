package no.uis.nio.sftp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Properties;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class FileSystemsTest {
  
  private static Properties testprops;
  
  @Rule
  public ExpectedException exception = ExpectedException.none();
  
  @BeforeClass
  public static void initProps() throws IOException {
    File testpropsFile = new File(System.getProperty("user.home"), "sftp-test.xml");
    if (testpropsFile.canRead()) {
      testprops = new Properties();
      testprops.loadFromXML(new FileInputStream(testpropsFile));
    }
  }
  
  @Test
  public void getFileSystemURI() throws Exception {
    Assume.assumeNotNull(testprops);
    
    URI uri = createTestURI("/");
    FileSystem fileSystem = FileSystems.getFileSystem(uri);
    assertThat(fileSystem, is(nullValue()));
  }
  
  @Test
  public void newFileSystem() throws Exception {
    Assume.assumeNotNull(testprops);
    URI uri = createTestURI("/");
    FileSystem fs = FileSystems.newFileSystem(uri, null);
    
    assertThat(fs, is(notNullValue()));
  }
  
  
  private URI createTestURI(String path) throws URISyntaxException {
    String userinfo = testprops.getProperty("sftp.userinfo");
    String host = testprops.getProperty("sftp.host");
    int port = Integer.parseInt(testprops.getProperty("sftp.port"));
    URI uri = new URI("sftp", userinfo, host, port, path, null, null);
    
    return null;
  }
}
