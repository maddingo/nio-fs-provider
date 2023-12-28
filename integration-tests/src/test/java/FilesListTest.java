import no.maddin.niofs.testutil.BasicTestContainer;
import no.maddin.niofs.testutil.FileUtils;
import no.maddin.niofs.testutil.SftpgoContainer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * We prepare a file structure and use the various providers list the files.
 */
@Testcontainers
public class FilesListTest {

    private static final String TESTDATA_RESOURCE = "/sftpgo-data";
    private static final File localDataFileRoot = FileUtils.classpathFile(FilesListTest.class, TESTDATA_RESOURCE);

    public static Stream<Arguments> data() {
        // anonymous class
        return Stream.of(
            Arguments.of(
                "sftp",
                (Supplier<BasicTestContainer>) () -> new SftpgoContainer(TESTDATA_RESOURCE)
            ),
            Arguments.of(
                "webdav",
                (Supplier<BasicTestContainer>) () -> new SftpgoContainer(TESTDATA_RESOURCE)
            )
        );
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("data")
    void listFiles(String protocol, Supplier<BasicTestContainer> containerSupplier) throws Exception {

        String dataSubDir = "/" + UUID.randomUUID();
        SortedSet<String> createdFiles = FileUtils.createFilesInDir(localDataFileRoot, dataSubDir, 10);
        try (BasicTestContainer container = containerSupplier.get()) {
            container.start();
            URI uri = container.getBaseUri(protocol);
            Path path = Paths.get(uri.resolve(dataSubDir));
            try (Stream<Path> paths = Files.list(path)) {
                SortedSet<String> foundFiles = paths
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toCollection(TreeSet::new));

                assertThat(foundFiles, equalTo(createdFiles));
            }
        }
    }

}
