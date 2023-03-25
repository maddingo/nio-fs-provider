package no.maddin.niofs.webdav;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Tests that require a running server.
 */
@Disabled
public class WebdavPathServerTest {

    private int webdavPort = -1;

    private File rootFolder;

    private String user = "user";

    private String password = "password";

//    private MiltonWebDAVFileServer server;

    /**
     * Start an embedded Webdav Server for testing.
     * TODO: Run bytemark/webdav in testContainers
     */
    @BeforeEach
    public void startWebdavServer() throws Exception {
//        rootFolder = Files.createTempDirectory(Paths.get(".", "target"), "webdav-test").toFile();
//        server = new MiltonWebDAVFileServer(rootFolder);
//        try (ServerSocket ssock = new ServerSocket(0, 1, new InetSocketAddress(0).getAddress())) {
//            webdavPort = ssock.getLocalPort();
//            server.setPort(webdavPort); // optional, defaults to 8080
//            server.getUserCredentials().put(user, password); // optional, defaults to no authentication
//            ssock.close(); // close before the server can re-bind to the random port.
//            server.start();
//        }
    }

    /**
     * Stop the embedded Webdav server.
     */
    @AfterEach
    public void stopWebDavServer() throws Exception {
//        server.stop();
    }

    /**
     * Tests {@link Files#createDirectories(Path, FileAttribute[])} with WebDavPath
     */
    @Test
    public void getCreateChildPath() throws Exception {
        URI uri = new URI("webdav", user + ':' + password,"localhost", webdavPort, "/a/b", null, null);
        Path path = Paths.get(uri);
        Path newPath = Files.createDirectories(path);
        assertThat(newPath, is(notNullValue()));
        File fileA = new File(rootFolder.getAbsolutePath(), "a");
        assertThat(fileA.exists(), is(true));
        assertThat(fileA.isDirectory(), is(true));
        File fileB = new File(fileA.getAbsolutePath(), "b");
        assertThat(fileB.exists(), is(true));
        assertThat(fileB.isDirectory(), is(true));
    }

    /**
     * Tests {@link Files#copy(Path, Path, CopyOption...)} with WebdavPath as target.
     */
    @Test
    public void copyFiles() throws Exception {
        File src = File.createTempFile("webdavtest", ".txt", new File("target"));
        try (FileWriter fw = new FileWriter(src)) {
            fw.append("test test");
        }

        URI uriTo = new URI("webdav", user + ':' + password,"localhost", webdavPort, "/webdav/test2/file.txt", null, null);

        Path pathTo = Paths.get(uriTo);
        Files.copy(src.toPath(), pathTo, StandardCopyOption.REPLACE_EXISTING);

        File fileWebdav = new File(rootFolder.getAbsolutePath(), "webdav");
        assertThat(fileWebdav.exists(), is(true));
        assertThat(fileWebdav.isDirectory(), is(true));
        File fileTest2 = new File(fileWebdav.getAbsolutePath(), "test2");
        assertThat(fileTest2.exists(), is(true));
        assertThat(fileTest2.isDirectory(), is(true));
        File fileTxt = new File(fileTest2.getAbsolutePath(), "file.txt");
        assertThat(fileTxt.exists(), is(true));
        assertThat(fileTxt.isFile(), is(true));

        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileTxt))) {
            content.append(br.readLine());
        }
        assertThat(content.toString(), is(equalTo("test test")));
    }

    @Test
    public void deleteNonexistingFile() throws Exception {
        String uriPath = String.format("/webdav/test2/file-%s.txt", UUID.randomUUID().toString());
        URI uri = new URI("webdav", user + ':' + password,"localhost", webdavPort, uriPath, null, null);

        Path path = Paths.get(uri);
//        exception.expect(NoSuchFileException.class);
        Files.delete(path);
    }

    @Test
    public void deleteFile() throws Exception {
        File targetFile = writeTestFile("webdav/test2/file.txt");
        URI uri = new URI("webdav", user + ':' + password,"localhost", webdavPort, "/webdav/test2/file.txt", null, null);
        Path path = Paths.get(uri);
//        Assumptions.assumeThat("File should exist prior to deleting it", Files.exists(path), is(true));

        // delete the file
        Files.delete(path);

//        assumeThat("File " + targetFile.getAbsolutePath() + " should have perished", targetFile.exists(), is(false));
    }

    @Test
    public void deleteWrongHost() throws Exception {

        URI uri = new URI("webdav", user + ':' + password,"non-existing-host", webdavPort, "/", null, null);
//        exception.expect(instanceOf(IOException.class));
        Path path = Paths.get(uri);
        Files.delete(path);
    }

    @Test
    public void readFileAttributes() throws Exception {
        File testFile = writeTestFile("testfile.txt");

        URI uri = new URI("webdav", user + ':' + password, "localhost", webdavPort, "/testfile.txt", null, null);

        BasicFileAttributes attrs = Files.readAttributes(Paths.get(uri), BasicFileAttributes.class);

        assertThat(attrs, is(notNullValue()));
        assertThat(attrs.size(), is(greaterThan(0L)));
        assertThat(attrs.isDirectory(), is(false));
        assertThat(attrs.isRegularFile(), is(true));
        assertThat(attrs.isSymbolicLink(), is(false));
        assertThat(attrs.isOther(), is(false));
        assertThat(attrs.lastAccessTime(), is(notNullValue()));
        assertThat(attrs.lastModifiedTime(), is(notNullValue()));
        assertThat(attrs.creationTime(), is(notNullValue()));
    }

    /**
     * Tests {@link newDirectoryStream(Path path, Filter<? super Path> filter)} 
     * and {@link readAttributes(Path path, Class<A> type, LinkOption... options)}
     * with WebDavPath
     */
    
    @Test
    public void testDirListing(TestReporter reporter) throws Exception {
    	
    	ArrayList<Path> files = new ArrayList<Path>(20);
    	
        URI uri = new URI("webdav", user + ':' + password,"localhost", webdavPort, "/", null, null);
        
        Path path = Paths.get(uri);
        Stream<Path> paths = Files.list(path);
		Iterator<Path> iter = paths.iterator();
		while (iter.hasNext()) {
			path = iter.next();			
			files.add(path);
		}
		
		paths.close();
		Collections.sort(files);
		
		/* list the directory */
		StringBuilder sb = new StringBuilder(1024);
		for(Path p : files) {						
			long size = Files.size(p);
			FileTime modtime = (FileTime) Files.getAttribute(p, "lastModifiedTime");						
			
			sb.append(p.getFileName().toString());
			sb.append('\t');
			sb.append(size);			
			if(modtime != null) {
				sb.append('\t');
				sb.append(LocalDate.ofInstant(modtime.toInstant(), ZoneId.systemDefault()));
			}
			sb.append(System.lineSeparator());
		}
		reporter.publishEntry("dir listing", sb.toString());        
    }

    
    private File writeTestFile(String filePath) throws IOException {
        File targetFile = new File(rootFolder.getAbsolutePath(), filePath);
        targetFile.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(targetFile)) {
            fw.append("test test, delete file");
        }
        return targetFile;
    }
}
