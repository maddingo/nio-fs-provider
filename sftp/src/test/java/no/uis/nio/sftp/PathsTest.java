package no.uis.nio.sftp;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.junit.Test;

public class PathsTest {

  //@Test
  public void getURI() throws Exception {
    URI uri = URI.create("sftp://2904630:password@lportal-test.uis.no:2222/");

    FileSystems.newFileSystem(uri, null);
    Path path = Paths.get(uri);
    
    assertThat(path, is(notNullValue()));
  }

  //@Test
  public void getNewURI() throws Exception {
    URI uri = URI.create("sftp://2904630:password@lportal-test.uis.no:2222/");

    Path path = Paths.get(uri);
    
    assertThat(path, is(notNullValue()));
  }
  
  //@Test
  public void getCreateChildPath() throws Exception {
    URI uri = URI.create("sftp://2904630:password@lportal-test.uis.no/~/test/");
    Path path = Paths.get(uri);
    Path newPath = Files.createDirectories(path);
    assertThat(newPath, is(notNullValue()));
  }
  
  @Test
  public void dummy() {
    assertTrue(true);
  }
}
