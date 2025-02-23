package no.maddin.niofs.sftp;

import com.jcraft.jsch.SftpATTRS;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PermissionConverterTest {

    private static final Constructor<SftpATTRS> SFTP_ATTRS_CONSTRUCTOR = sftpAttrsConstructor();

    public static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_READ), 0_400, "-r--------"),
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_WRITE), 0_200, "--w-------"),
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_EXECUTE), 0_100, "---x------"),
            Arguments.of(EnumSet.of(PosixFilePermission.GROUP_READ), 0_40, "----r-----"),
            Arguments.of(EnumSet.of(PosixFilePermission.GROUP_WRITE), 0_20, "-----w----"),
            Arguments.of(EnumSet.of(PosixFilePermission.GROUP_EXECUTE), 0_10, "------x---"),
            Arguments.of(EnumSet.of(PosixFilePermission.OTHERS_READ), 0_4, "-------r--"),
            Arguments.of(EnumSet.of(PosixFilePermission.OTHERS_WRITE), 0_2, "--------w-"),
            Arguments.of(EnumSet.of(PosixFilePermission.OTHERS_EXECUTE), 0_1, "---------x"),
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), 0_600, "-rw-------"),
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE), 0_500, "-r-x------"),
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE), 0_700, "-rwx------"),
            Arguments.of(EnumSet.of(PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE), 0_60, "----rw----"),
            Arguments.of(EnumSet.of(PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE), 0_50, "----r-x---"),
            Arguments.of(EnumSet.of(PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_EXECUTE), 0_70, "----rwx---"),
            Arguments.of(EnumSet.of(PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE), 0_6, "-------rw-"),
            Arguments.of(EnumSet.of(PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE), 0_5, "-------r-x"),
            Arguments.of(EnumSet.of(PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OTHERS_EXECUTE), 0_7, "-------rwx"),
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ), 0_444, "-r--r--r--"),
            Arguments.of(EnumSet.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_WRITE, PosixFilePermission.OTHERS_WRITE), 0_222, "--w--w--w-"),

            Arguments.of(EnumSet.allOf(PosixFilePermission.class), 0_777, "-rwxrwxrwx")
        );
    }

    /**
     * Check that the SFTP implementation of the permissions is ued correctly.
     */
    @ParameterizedTest(name = "{index}: {2}")
    @MethodSource({"data"})
    void testConvertToPosixFilePermission(Set<PosixFilePermission> permission, int expected, String permStringExpected) throws Exception {
        assertThat(SFTPFileSystemProvider.permissionsToMask(permission), is(expected));

        SftpATTRS attrs = SFTP_ATTRS_CONSTRUCTOR.newInstance();
        attrs.setPERMISSIONS(expected);
        String permString = attrs.getPermissionsString();

        assertThat(permString, is(permStringExpected));
    }

    private static @NotNull Constructor<SftpATTRS> sftpAttrsConstructor() {
        Constructor<SftpATTRS> constr = Arrays.stream(SftpATTRS.class.getDeclaredConstructors())
            .filter(c -> c.getParameterCount() == 0)
            .findAny()
            .map(c -> (Constructor<SftpATTRS>) c)
            .orElseThrow(() -> new NullPointerException("No constructor found"));

        constr.setAccessible(true);
        return constr;
    }

}
