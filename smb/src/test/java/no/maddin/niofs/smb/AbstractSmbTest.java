package no.maddin.niofs.smb;

import jcifs.Config;
import jcifs.util.LogStream;
import no.maddin.niofs.commons.AbstractTest;
import org.junit.jupiter.api.BeforeAll;

import java.io.InputStream;

public abstract class AbstractSmbTest extends AbstractTest {
    @BeforeAll
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
