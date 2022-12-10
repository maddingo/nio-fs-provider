package no.maddin.niofs.smb;

import jcifs.smb.SmbFile;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

public class DirectoryStreamIteratorTest extends AbstractSmbTest {

    public static Iterable<Object[]> data() {
        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] {
                "smb://127.0.0.1/public/My Documents/",
                new String[] {"smb://127.0.0.1/public/My Documents/Folder One/", "smb://127.0.0.1/public/My Documents/Folder Two/"}
        });
        return data;
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("data")
    public void directoryStreamIterator(String parentUrl, String[] childrenUrls) throws Exception {

        URL parent = new URL(parentUrl);
        String parentUri = new URI(parent.getProtocol(), parent.getAuthority(), parent.getPath(), null).toString();
        String[] childrenUris = new String[childrenUrls.length];
        for (int i = 0; i < childrenUrls.length; i++) {
            URL child = new URL(childrenUrls[i]);
            childrenUris[i] = new URI(child.getProtocol(), child.getAuthority(), child.getPath(), null).toString();
        }
        // ignore the test if the expected test file system does not exist
        for (String child : childrenUrls) {
            SmbFile smbf = new SmbFile(child);
            MatcherAssert.assertThat(smbf.canRead(), is(true));
        }

        Path remotePath = Paths.get(new URI(parentUri));
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(remotePath, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return !Files.isSymbolicLink(entry);
            }
        })) {
            for (Path path : directoryStream) {
                fileNames.add(path.toString());
            }
        }

        MatcherAssert.assertThat(fileNames, hasItems(childrenUris));
    }
}
