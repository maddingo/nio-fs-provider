package no.maddin.niofs.sftp;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests splitting Path parts.
 */
public class PartsTest {

    public static List<Object[]> data() {

        return Arrays.asList(
                new Object[] {".", Collections.singletonList(".")},
                new Object[] {"", Collections.singletonList("")},
                new Object[] {null, Collections.emptyList()},
                new Object[] {"/", Arrays.asList("", "")},
                new Object[] {"./", Arrays.asList(".", "")},
                new Object[] {"/~", Arrays.asList("", "~")},
                new Object[] {"aa", Collections.singletonList("aa")},
                new Object[] {"/aa/bb", Arrays.asList("", "aa", "bb")},
                new Object[] {"/aa/../bb", Arrays.asList("", "aa", "..", "bb")},
                new Object[] {"/aa/../bb.txt", Arrays.asList("", "aa", "..", "bb.txt")},
                new Object[] {"../", Arrays.asList("..", "")}
        );
    }

    @ParameterizedTest
    @MethodSource({"data"})
    public void splitDot(String input, List<String> result) {
        SFTPPath path = new SFTPPath(null, input);

        List<String> parts = path.getParts();

        assertThat(parts, is(equalTo(result)));
    }
}
