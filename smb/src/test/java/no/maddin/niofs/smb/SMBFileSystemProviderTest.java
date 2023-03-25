package no.maddin.niofs.smb;

import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static no.maddin.niofs.testutil.Matchers.hasRecordComponent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SMBFileSystemProviderTest {

    static Stream<Arguments> data() {

        return Stream.of(
            Arguments.of(
                "domain\\user:password",
                Map.of("USERNAME", "user1", "PASSWORD", "password1", "DOMAIN", "domain1"),
                allOf(
                    hasRecordComponent("username", equalTo("user")),
                    hasRecordComponent("password", equalTo("password")),
                    hasRecordComponent("domain", equalTo("domain"))
                )
            ),
            Arguments.of(
                "user:password",
                Map.of("USERNAME", "user1", "PASSWORD", "password1", "DOMAIN", "domain1"),
                allOf(
                    hasRecordComponent("username", equalTo("user")),
                    hasRecordComponent("password", equalTo("password")),
                    hasRecordComponent("domain", nullValue())
                )
            ),
            Arguments.of(
                "user", // missing password is ignored
                Map.of("USERNAME", "user1", "PASSWORD", "password1", "DOMAIN", "domain1"),
                allOf(
                    hasRecordComponent("username", equalTo("user1")),
                    hasRecordComponent("password", equalTo("password1")),
                    hasRecordComponent("domain", equalTo("domain1"))
                )
            ),
            Arguments.of(
                "",
                Map.of("USERNAME", "user1", "PASSWORD", "password1", "DOMAIN", "domain1"),
                allOf(
                    hasRecordComponent("username", equalTo("user1")),
                    hasRecordComponent("password", equalTo("password1")),
                    hasRecordComponent("domain", equalTo("domain1"))
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
