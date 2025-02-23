package no.maddin.niofs.sftp;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests splitting Path parts.
 */
class PartsTest {

    static Stream<Arguments> validPathsData() {

        return Stream.of(
            Arguments.of("/", Collections.emptyList()),
            Arguments.of("/aa", Collections.singletonList("aa")),
            Arguments.of("/aa/bb", Arrays.asList("aa", "bb")),
            Arguments.of("/aa/../bb", Collections.singletonList("bb")),
            Arguments.of("/aa/../bb", Collections.singletonList("bb")),
            Arguments.of("/aa/../bb/cc/../d.txt", Arrays.asList("bb", "d.txt")),
            Arguments.of(".", Collections.emptyList()),
            Arguments.of("./", Collections.emptyList()),
            Arguments.of("", Collections.emptyList()),
            Arguments.of("aa", Collections.singletonList("aa")),
            Arguments.of(null, Collections.emptyList())
        );
    }

    static Stream<Arguments> invalidPathsData() {
        return Stream.of(
//            Arguments.of(".", instanceOf(IllegalArgumentException.class)),
//            Arguments.of("", instanceOf(IllegalArgumentException.class)),
//            Arguments.of(null, instanceOf(IllegalArgumentException.class)),
//            Arguments.of("./", instanceOf(IllegalArgumentException.class)),
//            Arguments.of("aa", instanceOf(IllegalArgumentException.class)),
            Arguments.of("/aa/../../bb.txt", instanceOf(IllegalArgumentException.class)),
            Arguments.of("../", instanceOf(IllegalArgumentException.class))
        );
    }

    @ParameterizedTest
    @MethodSource({"validPathsData"})
    void validPaths(String input, List<String> result) {
        SFTPPath path = new SFTPPath(SFTPHost.SFTP_TEST_HOST, input);

        assertThat(path, hasProperty("parts", is(equalTo(result))));
    }

    @ParameterizedTest
    @MethodSource({"invalidPathsData"})
    void invalidPaths(String input, Matcher<Exception> expectedException) {
        try {
            new SFTPPath(SFTPHost.SFTP_TEST_HOST, input);

            fail("Call with '" + input + "' should have failed.");
        } catch (Exception ex) {
            assertThat(ex, is(expectedException));
        }
    }

    @Test
    void toStringTest() {
        SFTPHost host = new SFTPHost(new SFTPFileSystemProvider(), URI.create("sftp://localhost"));
        SFTPPath path = new SFTPPath(host, "/aa/bb/cc.txt");

        assertThat(path.toString(), is(equalTo("sftp://localhost/aa/bb/cc.txt")));
    }
}
