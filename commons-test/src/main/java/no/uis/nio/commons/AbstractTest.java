package no.uis.nio.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Assume;
import org.junit.BeforeClass;

public abstract class AbstractTest {

  protected static Properties testprops = new Properties();

  @BeforeClass
  public static void initProps() throws IOException {
    File testpropsFile = new File(System.getProperty("user.home"), "nio-test.xml");

    Assume.assumeTrue(testpropsFile.canRead());
    
    testprops.loadFromXML(new FileInputStream(testpropsFile));
  }
  
  protected URI createTestUri(String scheme, String host, int port, String path) throws URISyntaxException {
    String username = getProperty(scheme, "nio.user");
    String password = getProperty(scheme, "nio.password");
    
    return new URI(scheme, username + ':' + password, host, port, path, null, null);
  }
  
  public String getProperty(String schema, String key) {
    String value = testprops.getProperty(schema+'.'+key);
    if (value == null) {
      value = testprops.getProperty(key);
    }
    return value;
  }
}
