package no.uis.nio.smb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.util.LogStream;

import no.uis.nio.commons.AbstractTest;
import no.uis.nio.commons.CatalogCreatorMock;

import org.junit.BeforeClass;
import org.junit.Test;

public class CifsTest extends AbstractTest {

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
  public void testCatalogCreator() throws Exception {
    CatalogCreatorMock catalogCreator = new CatalogCreatorMock();
    
    catalogCreator.setPurgeOutputDir(true);
    URI baseDirUri = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/");
    
    Path baseDir = Paths.get(baseDirUri);
    assertThat(baseDir, is(notNullValue(Path.class)));
    
    catalogCreator.createCatalog(baseDir);
  }
  
  @Test
  public void testConnectToShare() throws Exception {
    URI baseDirUri = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/");
    SmbFile file = new SmbFile(baseDirUri.toURL());
    assertThat(file, is(notNullValue(SmbFile.class)));
    boolean canRead = file.canRead();
    assertThat(canRead, is(true));
  }
  
  @Test 
  public void testRelativize() throws Exception {
    URI uriA = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/a/a/a/");
    Path smbA = Paths.get(uriA);

    URI uriB = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/b/b/");
    Path smbB = Paths.get(uriB);
    
    Path relPath = smbA.relativize(smbB);
    
    assertThat(relPath, is(notNullValue(Path.class)));
    
    assertThat(relPath, is(instanceOf(SMBBasePath.class)));
    
    assertThat(relPath.toString(), is("..\\..\\..\\b\\b\\"));
  }
  
  @Test 
  public void testRelativize1() throws Exception {
    URI uriA = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/");
    Path smbA = Paths.get(uriA);

    URI uriB = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/temp/b/");
    Path smbB = Paths.get(uriB);
    
    Path relPath = smbA.relativize(smbB);
    
    assertThat(relPath, is(notNullValue(Path.class)));
    
    assertThat(relPath, is(instanceOf(SMBBasePath.class)));
    
    assertThat(relPath.toString(), is("b\\"));
  }

  @Test 
  public void testRelativeIterator() throws Exception {
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
  
  
}
