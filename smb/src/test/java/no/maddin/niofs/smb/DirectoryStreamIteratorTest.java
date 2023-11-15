package no.maddin.niofs.smb;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.nio.file.*;
import java.util.*;
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

        Set<URI> args = new TreeSet<>(Comparator.comparing(URI::toString));
        return Stream.of(
            Arguments.of(
                uri("smb://" + sambaAddress + "/public"),
                "/My Documents",
                set(
                    uri("smb://" + sambaAddress + "/public/My+Documents/Folder+One"),
                    uri("smb://" + sambaAddress + "/public/My+Documents/Folder+Two")
                ),
                (DirectoryStream.Filter<Path>) (entry) -> !entry.endsWith(".") && !entry.endsWith("..")
            ),
            Arguments.of(
                uri("smb://" + sambaAddress + "/public"),
                "/My Documents/Folder One",
                set(
                    uri("smb://" + sambaAddress + "/public/My+Documents/Folder+One/README.txt"),
                    uri("smb://" + sambaAddress + "/public/My+Documents/Folder+One/file1.txt")
                ),
                fileNameEndsWith(".txt")
            )

        );
    }

    @NotNull
    private static DirectoryStream.Filter<Path> fileNameEndsWith(String suffix) {
        return (entry) -> Optional.ofNullable(entry.getFileName())
            .map(Path::toString)
            .filter(f -> f.endsWith(suffix))
            .isPresent();
    }

    @ParameterizedTest
    @MethodSource("data")
    void directoryStreamIterator(URI shareUrl, String parentPath, SortedSet<URI> childrenUrls, DirectoryStream.Filter<Path> filter) throws Exception {

        Map<String, String> env = new HashMap<>();
        env.put("USERNAME", "admin");
        env.put("PASSWORD", "admin");
        FileSystem fileSystem = FileSystems.newFileSystem(shareUrl, env);
        assertThat(fileSystem, Matchers.is(notNullValue()));
        Path remotePath = fileSystem.getPath(parentPath);
        SortedSet<URI> fileNames = new TreeSet<>(Comparator.comparing(URI::toString));
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(remotePath, filter)) {
            for (Path path : directoryStream) {
                fileNames.add(path.toUri());
            }
        }

        assertThat(fileNames, equalTo(childrenUrls));
    }

    private static URI uri(String url) {
        return URI.create(url);
    }

    @SafeVarargs
    private static <T> SortedSet<T> set(T... elements) {
        SortedSet<T> set = new TreeSet<>(Comparator.comparing(Object::toString));
        Collections.addAll(set, elements);
        return set;
    }

}
