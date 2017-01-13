import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class PathServerOperations {

    private final String uriString;
    private final Matcher<?> expected;

    @Parameterized.Parameters(name="{index} {0} {1}")
    public static java.util.List<Object[]> data() {
        return Arrays.asList(
                new Object[] {"sftp://localhost/", is(true)},
                new Object[] {"sftp://localhost/testfile/", is(true)}
        );
    }

    public PathServerOperations(String uriString, Matcher<?> expected) {
        this.uriString = uriString;
        this.expected = expected;
    }

    private void startSftpServer() {

    }
}
