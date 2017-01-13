import no.maddin.niofs.sftp.SFTPPath;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class PathsGetUriIT {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final String uriString;
    private final Matcher<Path> testGetUriExpected;
    private final Matcher<? extends Throwable> exceptionExpectation;

    @Parameterized.Parameters(name="{index} {0} {1}")
    public static java.util.List<Object[]> data() {
        return Arrays.asList(
            new Object[] {
                    "sftp://localhost/",
                    null,
                    instanceOf(SFTPPath.class)
            },
            new Object[] {
                    "sftp://localhost/testfile/",
                    null,
                    instanceOf(SFTPPath.class)
            },
            new Object[] {
                    "sftp://localhost/../testfile/",
                    null,
                    instanceOf(SFTPPath.class)
            },
            new Object[] {
                    "sftp:/localhost/../testfile/",
                    instanceOf(IllegalArgumentException.class),
                    instanceOf(SFTPPath.class)
            }

        );
    }

    public PathsGetUriIT(String uriString, Matcher<? extends Throwable> exceptionExpectation, Matcher<Path> testGetUriExpected) {
        this.uriString = uriString;
        this.exceptionExpectation = exceptionExpectation;
        this.testGetUriExpected = testGetUriExpected;
    }

    @Test
    public void getURI() throws Exception {

        if (exceptionExpectation != null) {
            exception.expect(exceptionExpectation);
        }
        Path p = Paths.get(URI.create(this.uriString));

        assertThat(p, is(testGetUriExpected));
    }
}
