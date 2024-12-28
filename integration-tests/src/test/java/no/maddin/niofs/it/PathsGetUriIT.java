package no.maddin.niofs.it;

import no.maddin.niofs.sftp.SFTPPath;
import org.hamcrest.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PathsGetUriIT {

    public static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(
                "sftp://localhost/",
                null,
                allOf(
                    instanceOf(SFTPPath.class),
                    not(pathMatch("endsWith", "/"))
                )
            ),
            Arguments.of(
                "sftp://localhost/testfile/",
                null,
                allOf(
                    instanceOf(SFTPPath.class),
                    not(pathMatch("endsWith", "/testfile/"))
                )
            ),
            Arguments.of(
                "sftp://localhost/testfile/",
                null,
                allOf(
                    instanceOf(SFTPPath.class),
                    not(pathMatch("endsWith", "/testfile"))
                )
            ),
            Arguments.of(
                "sftp://localhost/testfile/",
                null,
                allOf(
                    instanceOf(SFTPPath.class),
                    pathMatch("endsWith", "testfile/")
                )
            ),
            Arguments.of(
                "sftp://localhost/testfile/",
                null,
                allOf(
                    instanceOf(SFTPPath.class),
                    pathMatch("endsWith", "testfile")
                )
            ),
            Arguments.of(
                "sftp://localhost/testfile",
                null,
                allOf(
                    instanceOf(SFTPPath.class),
                    pathMatch("endsWith", "testfile")
                )
            ),
            Arguments.of(
                "sftp://localhost/dir1/../testfile/",
                null,
                allOf(
                    instanceOf(SFTPPath.class),
                    pathMatch("endsWith", "testfile"),
                    pathMatch("endsWith", "testfile")
                )
            ),
            Arguments.of(
                "sftp:/localhost/../testfile/",
                instanceOf(IllegalArgumentException.class),
                nullValue()
            )
        );
    }

    @ParameterizedTest (name = "{index} {0} {2}")
    @MethodSource("data")
    void getURI(String uriString, Matcher<Exception> exceptionExpectation, Matcher<Path> testGetUriExpected) {

        Exception tr = null;
        try {
            Path p = Paths.get(URI.create(uriString));
            assertThat(p, is(testGetUriExpected));
        } catch (Exception ex) {
            tr = ex;
        }
        assertThat(tr, matchesException(exceptionExpectation));
    }

    private Matcher<Exception> matchesException(Matcher<Exception> exceptionExpectation) {
        return new DiagnosingMatcher<Exception>() {
            @Override
            protected boolean matches(Object item, Description mismatchDescription) {
                if (exceptionExpectation == null) {
                    return Condition.matched(item, mismatchDescription).matching(nullValue());
                } else {
                    return Condition.matched(item, mismatchDescription).and((value, mismatch) -> {
                        if (value instanceof Exception) {
                            mismatch.appendText("is an exception");
                            return Condition.matched((Exception) value, mismatch);
                        } else {
                            mismatch.appendText("not an exception");
                            return Condition.notMatched();
                        }
                    }).matching(exceptionExpectation);
                }
            }

            @Override
            public void describeTo(Description description) {
                if (exceptionExpectation == null) {
                    description.appendText("expects no exception");
                } else {
                    description.appendText("expects exception ").appendValue(exceptionExpectation);
                }
            }
        };
    }

    private static TypeSafeDiagnosingMatcher<Path> pathMatch(String testMethod, Object arg) {
        return new TypeSafeDiagnosingMatcher<Path>() {

            @Override
            protected boolean matchesSafely(Path item, Description mismatchDescription) {
                try {
                    Method m = item.getClass().getMethod(testMethod, arg.getClass());
                    return (Boolean)m.invoke(item, arg);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("path.").appendText(testMethod).appendText("(").appendValue(arg).appendText(")");
            }
        };
    }

    private static TypeSafeDiagnosingMatcher<Path> endsWith(String endsWith) {
        return new TypeSafeDiagnosingMatcher<Path>() {

            @Override
            protected boolean matchesSafely(Path item, Description mismatchDescription) {
                return item.endsWith(endsWith);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("path ending with ").appendValue(endsWith);
            }
        };
    }
}