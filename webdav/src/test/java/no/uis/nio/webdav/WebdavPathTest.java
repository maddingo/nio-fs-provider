package no.uis.nio.webdav;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class WebdavPathTest {

	@Test
	public void getURI() throws Exception {
		URI uri = URI
				.create("webdav://2910195:password@lportal-test.uis.no");

		FileSystems.newFileSystem(uri, null);
		Path path = Paths.get(uri);

		assertThat(path, is(notNullValue()));
	}

	@Test
	public void getNewURI() throws Exception {
		URI uri = URI
				.create("webdav://2910195:password@lportal-test.uis.no");

		Path path = Paths.get(uri);

		assertThat(path, is(notNullValue()));
	}

	@Test
	public void getCreateChildPath() throws Exception {
		URI uri = URI.create("webdav://2910195:password@lportal-test.uis.no/webdav/test2");
		Path path = Paths.get(uri);
		Path newPath = Files.createDirectories(path);
		assertThat(newPath, is(notNullValue()));
	}

	@Test
	public void deleteFile() throws Exception {
		URI uri = URI.create("webdav://2910195:password@lportal-test.uis.no/webdav/test/file.txt");
		Path path = Paths.get(uri);
		Files.delete(path);
	}
	
	@Test
	public void copyFiles() throws Exception {
		URI uri = URI.create("webdav://2910195:password@lportal-test.uis.no/webdav/test/file.txt");
		URI uriTo = URI.create("webdav://2910195:password@lportal-test.uis.no/webdav/test2/file.txt");
		Path path = Paths.get(uri);
		Path pathTo = Paths.get(uriTo);
		Files.createDirectory(path);
		Files.copy(path, pathTo);
	}
}
