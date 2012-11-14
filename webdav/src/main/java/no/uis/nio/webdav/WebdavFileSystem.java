package no.uis.nio.webdav;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;

public class WebdavFileSystem extends FileSystem {

	private final FileSystemProvider provider;
	private final URI serverUri;
	private final int port;
	private final String host;
	private final String password;
	private final String username;

	public WebdavFileSystem(WebdavFileSystemProvider provider, URI serverUri) {
		this.provider = provider;
		this.serverUri = serverUri;
		this.host = serverUri.getHost();
		this.port = serverUri.getPort();
		String[] ui = serverUri.getUserInfo().split(":");
		this.username = ui[0];
		this.password = ui[1];
	}

	@Override
	public FileSystemProvider provider() {
		return provider;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<FileStore> getFileStores() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getPath(String first, String... more) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSeparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WatchService newWatchService() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Check if one filesystem is equal to another. Checks Host, username and
	 * Port
	 * 
	 * @param Object
	 *            other
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof WebdavFileSystem)) {
			throw new IllegalArgumentException(
					"Argument must be of instance WebdavFileSystem");
		}
		WebdavFileSystem current = (WebdavFileSystem) other;
		if (current.host.equals(this.host)
				&& current.username.equals(this.username)
				&& current.port == this.port) {
			return true;
		} else {
			return false;
		}
	}

	public String getUserName() {
		return this.username;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getPassword() {
		return this.password;
	}

	protected Sardine getSardine() throws IOException {

		Sardine sardine;
		SSLSocketFactory sslSocketFactory = null;
		if (serverUri.getScheme().equals("webdavs")) {
			sslSocketFactory = SSLSocketFactory.getSocketFactory();
		}
		sardine = SardineFactory.begin(username, password, sslSocketFactory,
				null, port);

		return sardine;
	}
}
