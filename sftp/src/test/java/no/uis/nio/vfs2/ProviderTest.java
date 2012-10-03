package no.uis.nio.vfs2;

import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;


public class ProviderTest {
  
  @Test
  public void testNewFileSystem() throws Exception {

    URI uri = URI.create("sftp://lportal-test.uis.no/");
    Map<String, String> env = new HashMap<String, String>();
    try (FileSystem ftpFs = FileSystems.newFileSystem(uri, env)) {
      assertThat(ftpFs, is(notNullValue()));
    }
  }
  
  @Test
  public void testOpenFile() throws Exception {
    URI uri = URI.create("sftp://lportal-test.uis.no/");
    Map<String, String> env = new HashMap<String, String>();
    try (FileSystem ftpFs = FileSystems.newFileSystem(uri, env)) {
      assertThat(ftpFs, is(notNullValue()));
      assertThat(ftpFs.isOpen(), is(true));
    }
  }
}
