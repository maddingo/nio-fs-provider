package no.maddin.niofs.smb;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
public class DirectoryStreamIteratorTest {

    @Container
    public static SambaContainer samba = new SambaContainer("target/test-classes/smb");

    static Stream<Arguments> data() {

        String sambaAddress = samba.getGuestIpAddress();
//        int port139 = samba.getMappedPort(139);
//        int port445 = samba.getMappedPort(445);

        return Stream.of(
            Arguments.of(
                uri("smb://" + sambaAddress + "/public"),
                Set.of(uri("smb://" + sambaAddress + "/public/My+Documents/Folder+One/"), uri("smb://" + sambaAddress + "/public/My+Documents/Folder+Two/")))
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void directoryStreamIterator(URI shareUrl, Set<URI> childrenUrls) throws Exception {

        FileSystem fileSystem = FileSystems.newFileSystem(shareUrl, Map.of("USERNAME", "admin", "PASSWORD", "admin"));
        assertThat(fileSystem, Matchers.is(notNullValue()));
        Path remotePath = fileSystem.getPath("/My Documents");
        Set<URI> fileNames = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(remotePath, entry -> !Files.isSymbolicLink(entry) && !entry.startsWith("."))) {
            for (Path path : directoryStream) {
                fileNames.add(path.toUri());
            }
        }

        assertThat(fileNames, equalTo(childrenUrls));
    }

    private static URI uri(String url) {
        return URI.create(url);
    }
}
