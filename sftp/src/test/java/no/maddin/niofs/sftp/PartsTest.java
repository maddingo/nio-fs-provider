package no.maddin.niofs.sftp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests splitting Path parts.
 */
@RunWith(Parameterized.class)
public class PartsTest {

    private final String input;
    private final List<String> result;

    @Parameterized.Parameters(name = "{index}: {0} {1}")
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

    public PartsTest(String input, List<String> result) {
        this.input = input;
        this.result = result;
    }

    @Test
    public void splitDot() {
        SFTPPath path = new SFTPPath(null, input);

        List<String> parts = path.getParts();

        assertThat(parts, is(equalTo(this.result)));
    }
}
