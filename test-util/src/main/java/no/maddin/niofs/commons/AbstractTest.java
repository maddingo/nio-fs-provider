package no.maddin.niofs.commons;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Base class for unit tests.
 * @deprecated try to get rid of this. Test should be self-contained without external configuration.
 */
@Deprecated
public abstract class AbstractTest {

    /**
     * Test parameters provided by <code>${user.dir}/src/test/nio-test.xml</code>.
     */
    protected static Properties testprops = new Properties();

    @BeforeAll
    public static void initProps() throws IOException {
        File testpropsFile = Paths.get(System.getProperty("user.dir"), "src", "test", "nio-test.xml").toFile();

        Assumptions.assumingThat(testpropsFile.canRead(), () -> testprops.loadFromXML(new FileInputStream(testpropsFile)));
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
