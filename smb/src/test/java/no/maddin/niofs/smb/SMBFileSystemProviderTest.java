package no.maddin.niofs.smb;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SMBFileSystemProviderTest {

    static Stream<Arguments> data() {

        Map<String, String> env = new HashMap<>();
        env.put("USERNAME", "user1");
        env.put("PASSWORD", "password1");
        env.put("DOMAIN", "domain1");
        return Stream.of(
            Arguments.of(
                "domain\\user:password",
                env,
                allOf(
                    hasProperty("username", equalTo("user")),
                    hasProperty("password", equalTo("password")),
                    hasProperty("domain", equalTo("domain"))
                )
            ),
            Arguments.of(
                "user:password",
                env,
                allOf(
                    hasProperty("username", equalTo("user")),
                    hasProperty("password", equalTo("password")),
                    hasProperty("domain", nullValue())
                )
            ),
            Arguments.of(
                "user", // missing password is ignored
                env,
                allOf(
                    hasProperty("username", equalTo("user1")),
                    hasProperty("password", equalTo("password1")),
                    hasProperty("domain", equalTo("domain1"))
                )
            ),
            Arguments.of(
                "",
                env,
                allOf(
                    hasProperty("username", equalTo("user1")),
                    hasProperty("password", equalTo("password1")),
                    hasProperty("domain", equalTo("domain1"))
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void principal(String userInfo, Map<String, String> map, Matcher<SMBShare.UsernamePassword> expected) {
        SMBShare.UsernamePassword unp = SMBFileSystemProvider.principal(userInfo, map);

        assertThat(unp, expected);
    }
}
