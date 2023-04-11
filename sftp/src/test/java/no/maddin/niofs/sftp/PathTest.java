package no.maddin.niofs.sftp;

import org.hamcrest.Condition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

class PathTest {

    static Stream<Arguments> validPathData() {
        return Stream.of(
            Arguments.of(
                URI.create("sftp://172.0.2.3/"),
                allOf(
                    instanceOf(SFTPPath.class),
                    hasProperty("absolute", equalTo(true)),
                    hasProperty("nameCount", equalTo(0))
                )
            )
        );
    }

    static Stream<Arguments> normalizeData() {
        return Stream.of(
            Arguments.of(
                URI.create("sftp://localhost/test/text.txt"),
                allOf(
                    instanceOf(SFTPPath.class),
                    hasProperty("pathString", equalTo("/test/text.txt")),
                    pathStartsWith("sftp://localhost/test")
                ),
                allOf(
                    instanceOf(SFTPPath.class),
                    hasProperty("pathString", equalTo("/test"))
                )
            ),
            Arguments.of(
                URI.create("sftp://localhost/text.txt"),
                allOf(
                    instanceOf(SFTPPath.class),
                    hasProperty("pathString", equalTo("/text.txt"))
                ),
                allOf(
                    instanceOf(SFTPPath.class),
                    hasProperty("pathString", equalTo("/"))
                )
            ),
            Arguments.of(
                URI.create("sftp://localhost/test/../text.txt"),
                allOf(
                    instanceOf(SFTPPath.class),
                    hasProperty("pathString", equalTo("/text.txt")),
                    pathStartsWith("sftp://localhost/")
                ),
                allOf(
                    instanceOf(SFTPPath.class),
                    hasProperty("pathString", equalTo("/"))
                )
            )
        );
    }

    static Stream<Arguments> invalidPathData() {
        return Stream.of(
            Arguments.of(
                URI.create("sftp://localhost/../test"),
                allOf(
                    instanceOf(IllegalArgumentException.class)
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("validPathData")
    public void validPath(URI uri, Matcher<Path> expectedResult) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(serverUri(uri), Collections.emptyMap())) {
            assertThat(fs, hasProperty("open", equalTo(true)));
            Path path = Paths.get(uri);
            assertThat(path, is(expectedResult));
        }
    }

    @ParameterizedTest
    @MethodSource("normalizeData")
    void normalize(URI uri, Matcher<Path> normalizedResult, Matcher<Path> parentMatcher) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(serverUri(uri), Collections.emptyMap())) {
            assertThat(fs, hasProperty("open", equalTo(true)));
            Path path = Paths.get(uri);
            Path normalizedPath = path.normalize();
            assertThat(normalizedPath, is(normalizedResult));
            Path parentPath = path.getParent();
            assertThat(parentPath, is(parentMatcher));
        }
    }

    @ParameterizedTest
    @MethodSource("invalidPathData")
    void invalidPaths(URI uri, Matcher<Exception> expectedException) {
        try (FileSystem fs = FileSystems.newFileSystem(serverUri(uri), Collections.emptyMap())) {
            assertThat(fs, hasProperty("open", equalTo(true)));
            Path path = Paths.get(uri);
            fail("Path " + path + " should be invalid for " + uri);
        } catch(Exception ex) {
            assertThat(ex, is(expectedException));
        }
    }

    @NotNull
    private static TypeSafeDiagnosingMatcher<Path> pathStartsWith(String startsWith) {
        return new TypeSafeDiagnosingMatcher<Path>() {
            @Override
            protected boolean matchesSafely(Path item, Description mismatchDescription) {
                return Condition.matched(item.startsWith(startsWith), mismatchDescription).matching(equalTo(true));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("path that starts with ").appendValue(startsWith);
            }
        };
    }

    @NotNull
    private static URI serverUri(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getRawUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.valueOf(uri));
        }
    }

}
