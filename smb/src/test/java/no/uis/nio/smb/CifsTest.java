package no.uis.nio.smb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import no.uis.nio.commons.AbstractTest;
import no.uis.nio.commons.CatalogCreatorMock;

import org.junit.Test;

public class CifsTest extends AbstractTest {

  @Test
  public void testCatalogCreator() throws Exception {
    CatalogCreatorMock catalogCreator = new CatalogCreatorMock();
    
    catalogCreator.setPurgeOutputDir(true);
    URI baseDirUri = createTestUri("smb", "wsapps-test01.uis.no", -1, "/d$/");
    
    Path baseDir = Paths.get(baseDirUri);
    assertThat(baseDir, is(notNullValue(Path.class)));
    
    catalogCreator.createCatalog(baseDir);
  }
}
