package no.maddin.niofs.webdav;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Tests that require a running server.
 * If we map the host file, we get problems with the file permissions, so we keep everything in the container.
 */
@Testcontainers
public class WebdavPathServerTest {

    @Container
    public static WebdavContainer webdav = new WebdavContainer();

    private static final File rootFolder = classpathFile();

    /**
     * Tests {@link Files#createDirectories(Path, FileAttribute[])} with WebDavPath
     */
    @Test
    public void getCreateChildPath() throws Exception {
        URI uri = URI.create(webdav.getWebdavUrl() + "/a/b");
        Path path = Paths.get(uri);
        Path newPath = Files.createDirectories(path);
        assertThat(newPath, is(notNullValue()));
        ExecResult result = webdav.execInContainer("ls", "-1", "-R", "/tmp");
        assertThat(result.getStdout(), equalTo("/tmp:\na\n\n/tmp/a:\nb\n\n/tmp/a/b:\n"));
    }

    /**
     * Tests {@link Files#copy(Path, Path, CopyOption...)} with WebdavPath as target.
     */
//    @Test
    public void copyFiles() throws Exception {
        File src = File.createTempFile("webdavtest", ".txt", new File("target"));
        try (FileWriter fw = new FileWriter(src)) {
            fw.append("test test");
        }

        URI uriTo = URI.create(webdav.getWebdavUrl() + "/test2/file.txt");

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

//    @Test
    public void deleteNonexistingFile() throws Exception {
        String uriPath = String.format("/webdav/test2/file-%s.txt", UUID.randomUUID());
        URI uri = URI.create(webdav.getWebdavUrl());

        Path path = Paths.get(uri);
//        exception.expect(NoSuchFileException.class);
        Files.delete(path);
    }

//    @Test
    public void deleteFile() throws Exception {
        File targetFile = writeTestFile("webdav/test2/file.txt");
        URI uri = URI.create(webdav.getWebdavUrl() + "/webdav/test2/file.txt");
        Path path = Paths.get(uri);
//        Assumptions.assumeThat("File should exist prior to deleting it", Files.exists(path), is(true));

        // delete the file
        Files.delete(path);

//        assumeThat("File " + targetFile.getAbsolutePath() + " should have perished", targetFile.exists(), is(false));
    }

//    @Test
    public void deleteWrongHost() throws Exception {

        URI uri = new URI("webdav", "user:password","non-existing-host", 2022, "/", null, null);
//        exception.expect(instanceOf(IOException.class));
        Path path = Paths.get(uri);
        Files.delete(path);
    }

//    @Test
    public void readFileAttributes() throws Exception {
        File testFile = writeTestFile("testfile.txt");

        URI uri = URI.create(webdav.getWebdavUrl() + "/testfile.txt");

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
     * Tests {@link java.nio.file.spi.FileSystemProvider#newDirectoryStream(Path, DirectoryStream.Filter)}
     * and {@link java.nio.file.spi.FileSystemProvider#readAttributes(Path, String, LinkOption...)}
     * with WebDavPath
     */

//    @Test
    public void testDirListing(TestReporter reporter) throws Exception {

    	ArrayList<Path> files = new ArrayList<Path>(20);

        URI uri = URI.create(webdav.getWebdavUrl());

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
				sb.append(LocalDate.from(modtime.toInstant()));
			}
			sb.append(System.lineSeparator());
		}
		reporter.publishEntry("dir listing", sb.toString());
    }

    /**
     * Not needed anymore, we assert the result inside the container.
     */
    @Deprecated
    private static File classpathFile() {
        try {
            return Files.createTempDirectory("test").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
