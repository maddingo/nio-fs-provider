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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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

    Path outDir = createOutPath(baseDir, "TEST", 2013, "VÅR", "X");
    
    assertThat(outDir.toString(), endsWith("TEST/2013/VÅR/X/"));
    
    catalogCreator.createCatalog(outDir);
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
  
  private static Path createOutPath(Path base, String type, int year,
      String fsSemester, String language) {
    Path out = base.
        resolve(type.toString()+"/").
        resolve(String.valueOf(year)+"/").
        resolve(fsSemester+"/").
        resolve(language+"/");

    return out;
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
}
