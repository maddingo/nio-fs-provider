import no.maddin.niofs.sftp.SFTPPath;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PathsGetUriIT {

    public static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(
                    "sftp://localhost/",
                    null,
                    instanceOf(SFTPPath.class)
            ),
            Arguments.of(
                    "sftp://localhost/testfile/",
                    null,
                    instanceOf(SFTPPath.class)
            ),
            Arguments.of(
                    "sftp://localhost/../testfile/",
                    null,
                    instanceOf(SFTPPath.class)
            ),
            Arguments.of(
                    "sftp:/localhost/../testfile/",
                    instanceOf(IllegalArgumentException.class),
                    instanceOf(SFTPPath.class)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void getURI(String uriString, Matcher<? extends Throwable> exceptionExpectation, Matcher<Path> testGetUriExpected) throws Exception {

        Throwable tr = null;
        try {
            Path p = Paths.get(URI.create(uriString));
            assertThat(p, is(testGetUriExpected));
        } catch (Exception ex) {
            tr = ex;
        }
        if (exceptionExpectation != null) {
            exceptionExpectation.matches(tr);
        }
    }
}
