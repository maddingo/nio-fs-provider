package no.uis.nio.vfs2;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;


public class ProviderTest {
  
  @Test
  public void testgetSftpUriPath() throws Exception {

    URI uri = URI.create("sftp://lportal-test.uis.no/");
    Path sftpPath = Paths.get(uri);
    assertThat(sftpPath, is(notNullValue()));
  }
}
