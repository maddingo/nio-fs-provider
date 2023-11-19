import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

public class PathServerOperationsIT {

    public static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of("sftp://localhost/", is(true)),
            Arguments.of("sftp://localhost/testfile/", is(true))
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void startSftpServer(String uriString, Matcher<?> expected) {

    }
}
