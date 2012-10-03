package no.uis.nio.webdav;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;


public class ProviderTest {
  
  @Test
  public void getSchema() {
	  WebdavFileSystemProvider wfs = new WebdavFileSystemProvider();
	  String schema = wfs.getScheme();
	  assertThat(schema, is("webdav"));
  }
}
