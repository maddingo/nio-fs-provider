import no.maddin.niofs.testutil.BasicTestContainer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.io.FileMatchers.anExistingFile;

/** Create a local file system and expose it through a number of FileSystems.
 * Validate the result on the local file system
 */
public class FileOperationsIT {


    public static Stream<Arguments> copyData() {
        return Stream.empty();
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("copyData")
    void copyFile(Supplier<BasicTestContainer> containerSupplier, String sourceUri, String targetUri, File targetFile) throws Exception {
        Path sourcePath = Paths.get(URI.create(sourceUri));
        Path targetPath = Paths.get(URI.create(targetUri));
        Files.copy(sourcePath, targetPath);
        assertThat(targetFile, anExistingFile());
    }

}
