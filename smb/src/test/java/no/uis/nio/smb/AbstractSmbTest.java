package no.uis.nio.smb;

import jcifs.Config;
import jcifs.util.LogStream;
import no.uis.nio.commons.AbstractTest;
import org.junit.BeforeClass;

import java.io.InputStream;

public abstract class AbstractSmbTest extends AbstractTest {
    @BeforeClass
    public static void init() throws Exception {
        InputStream config = DirectoryStreamIteratorTest.class.getResourceAsStream("/jcifs-test-config.properties");
        if (config != null) {
            Config.load(config);
            int loglevel = Config.getInt("jcifs.util.loglevel", Integer.MIN_VALUE);
            if (loglevel != Integer.MIN_VALUE) {
                LogStream.setLevel(loglevel);
            }
        }
        Config.registerSmbURLHandler();
    }


}
