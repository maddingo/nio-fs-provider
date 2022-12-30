package no.maddin.niofs.sftp;

import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
            Arguments.of("/", List.of()),
            Arguments.of("/aa", List.of("aa")),
            Arguments.of("/aa/bb", List.of("aa", "bb")),
            Arguments.of("/aa/../bb", List.of("bb")),
            Arguments.of("/aa/../bb", List.of("bb")),
            Arguments.of("/aa/../bb/cc/../d.txt", List.of("bb", "d.txt"))
        );
    }

    static Stream<Arguments> invalidPathsData() {
        return Stream.of(
            Arguments.of(".", instanceOf(IllegalArgumentException.class)),
            Arguments.of("", instanceOf(IllegalArgumentException.class)),
            Arguments.of(null, instanceOf(IllegalArgumentException.class)),
            Arguments.of("./", instanceOf(IllegalArgumentException.class)),
            Arguments.of("aa", instanceOf(IllegalArgumentException.class)),
            Arguments.of("/aa/../../bb.txt", instanceOf(IllegalArgumentException.class)),
            Arguments.of("../", instanceOf(IllegalArgumentException.class))
        );
    }

    @ParameterizedTest
    @MethodSource({"validPathsData"})
    void validPaths(String input, List<String> result) {
        SFTPPath path = new SFTPPath(null, input);

        assertThat(path, hasProperty("parts", is(equalTo(result))));
    }

    @ParameterizedTest
    @MethodSource({"invalidPathsData"})
    void invalidPaths(String input, Matcher<Exception> expectedException) {
        try {
            SFTPPath path = new SFTPPath(null, input);

            fail("Call with '" + input + "' should have failed.");
        } catch (Exception ex) {
            assertThat(ex, is(expectedException));
        }
    }
}
