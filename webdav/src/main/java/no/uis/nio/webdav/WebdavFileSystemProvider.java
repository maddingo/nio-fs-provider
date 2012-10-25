package no.uis.nio.webdav;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.http.conn.ssl.SSLSocketFactory;
import com.googlecode.sardine.*;

public class WebdavFileSystemProvider extends FileSystemProvider {

	private static final int DEFAULT_PORT = 22;
	private Map<URI, WebdavFileSystem> hosts = new HashMap<URI, WebdavFileSystem>();

	public void put(Path fileFrom, Path fileTo) throws IOException {
		if ((fileTo instanceof WebdavPath) == false) {
			throw new IllegalArgumentException(fileTo.toString());
		}

		Path from = fileFrom;
		WebdavPath webdavPathTo = (WebdavPath) fileTo;

		WebdavFileSystem webdavHost = (WebdavFileSystem) fileTo.getFileSystem();

		String username = webdavHost.getUserName();
		String password = webdavHost.getPassword();
		String host = webdavHost.getHost();

		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		Sardine webdav = SardineFactory.begin(username, password,
				sslSocketFactory);
		String pathTo = webdavPathTo.getPathString();

		try {
			webdav.put("https://" + host + pathTo, Files.readAllBytes(from));
		} catch (Exception e) {
			throw new IOException();
		}
	}

	@Override
	public void copy(Path fileFrom, Path fileTo, CopyOption... arg2)
			throws IOException {
		if ((fileTo instanceof WebdavPath) == false) {
			throw new IllegalArgumentException(fileTo.toString());
		}
		
		WebdavPath webdavPathFrom = (WebdavPath) fileFrom;
		WebdavPath webdavPathTo = (WebdavPath) fileTo;

		WebdavFileSystem webdavHostTo = (WebdavFileSystem) fileTo.getFileSystem();

		String username = webdavHostTo.getUserName();
		String password = webdavHostTo.getPassword();
		String host = webdavHostTo.getHost();

		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		Sardine webdav = SardineFactory.begin(username, password,
				sslSocketFactory);
		String pathTo = webdavPathTo.getPathString();
		String pathFrom = webdavPathFrom.getPathString();

		try {
			webdav.copy("https://" + host + pathTo, "https://" + host + pathFrom);
		} catch (Exception e) {
			throw new IOException();
		}
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... arg1)
			throws IOException {
	    if ((dir instanceof WebdavPath) == false) {
	        throw new IllegalArgumentException(dir.toString());
	      }

		WebdavPath webdavPath = (WebdavPath) dir;

		WebdavFileSystem webdavHost = (WebdavFileSystem) dir.getFileSystem();

	      String username = webdavHost.getUserName();
	      String password = webdavHost.getPassword();
		String host = webdavHost.getHost();
	      
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		Sardine webdav = SardineFactory.begin(username, password,
				sslSocketFactory);
		String path = webdavPath.getPathString();
	      try {
			webdav.createDirectory("https://" + host + path);
	      } catch (Exception e) {
	    	  throw new IOException();
	      }
	}

	@Override
	public void delete(Path dir) throws IOException {
	    if ((dir instanceof WebdavPath) == false) {
	        throw new IllegalArgumentException(dir.toString());
	      }

		WebdavPath webdavPath = (WebdavPath) dir;
		WebdavFileSystem webdavHost = (WebdavFileSystem) dir.getFileSystem();

	      String username = webdavHost.getUserName();
	      String password = webdavHost.getPassword();
		String host = webdavHost.getHost();

		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		Sardine webdav = SardineFactory.begin(username, password,
				sslSocketFactory);
		String path = webdavPath.getPathString();
	      
	      try {
			webdav.delete("https://" + host + path);
	      } catch (Exception e) {
	    	  throw new IOException();
	      }

	}

	@Override
	public FileSystem getFileSystem(URI uri) {
		try {
			return getWebdavHost(uri, true);
		} catch (Exception ex) {
			throw new FileSystemNotFoundException(uri.toString());
	}
	}

	@Override
	public Path getPath(URI uri) {
		WebdavFileSystem host;
		try {
			host = getWebdavHost(uri, true);
		} catch (URISyntaxException e) {
			throw new FileSystemNotFoundException(uri.toString());
		}

		if (host != null) {
			return new WebdavPath(host, uri.getPath());
		}
		return null;
	}

	private WebdavFileSystem getWebdavHost(URI uri, boolean create)
			throws URISyntaxException {
		String host = uri.getHost();
		int port = uri.getPort();
		if (port == -1) {
			port = DEFAULT_PORT;
		}
		String userInfo = uri.getUserInfo();
		URI serverUri = new URI(getScheme(), userInfo, host, port, null, null,
				null);

		synchronized (hosts) {
			WebdavFileSystem fs = hosts.get(serverUri);
			if (fs == null && create) {
				fs = new WebdavFileSystem(this, serverUri);
				hosts.put(serverUri, fs);
			}
			return fs;
		}
	}

	@Override
	public String getScheme() {
		return "webdav";
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path arg0,
			Class<V> arg1, LinkOption... arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileStore getFileStore(Path arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkAccess(Path arg0, AccessMode... arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isHidden(Path arg0) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSameFile(Path arg0, Path arg1) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void move(Path arg0, Path arg1, CopyOption... arg2)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public SeekableByteChannel newByteChannel(Path arg0,
			Set<? extends OpenOption> arg1, FileAttribute<?>... arg2)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path arg0,
			Filter<? super Path> arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env)
			throws IOException {
		try {
			return getWebdavHost(uri, true);
		} catch (URISyntaxException e) {
			throw new FileSystemException(e.toString());
		}
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path arg0,
			Class<A> arg1, LinkOption... arg2) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> readAttributes(Path arg0, String arg1,
			LinkOption... arg2) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(Path arg0, String arg1, Object arg2,
			LinkOption... arg3) throws IOException {
		// TODO Auto-generated method stub

	}

	protected Path uriToPath(URI uri) {
		String scheme = uri.getScheme();
		if (scheme == null || !getScheme().equalsIgnoreCase(scheme)) {
			throw new IllegalArgumentException("unsupported scheme " + scheme);
		}
		return getPath(uri);
	}

}
