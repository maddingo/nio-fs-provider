/*
 Copyright 2012-2013 University of Stavanger, Norway

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package no.uis.nio.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Assume;
import org.junit.BeforeClass;

/**
 * Base class for unit tests.
 */
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
