package no.uis.nio.sftp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.junit.Test;


public class FileSystemsTest {
  
  @Test
  public void getFileSystemURI() throws Exception {
    URI uri = URI.create("sftp://2904630:password@lportal-test.uis.no:2222/");
    FileSystem fileSystem = FileSystems.getFileSystem(uri);
    assertThat(fileSystem, is(nullValue()));
  }
  
  @Test
  public void newFileSystem() throws Exception {
    URI uri = URI.create("sftp://2904630:password@lportal-test.uis.no:2222/");
    FileSystem fs = FileSystems.newFileSystem(uri, null);
    
    assertThat(fs, is(notNullValue()));
  }
}
