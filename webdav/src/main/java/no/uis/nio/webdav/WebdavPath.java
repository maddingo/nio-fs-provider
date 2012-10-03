package no.uis.nio.webdav;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

public class WebdavPath implements Path {

	private static final String DEFAULT_ROOT_PATH = "";
	private static final String PATH_SEP = "/";
	private final String path;
	private final WebdavFileSystem host;

	public WebdavPath(WebdavFileSystem webdavHost, String path) {
		this.host = webdavHost;
		if (path == null || path.trim().isEmpty()) {
			this.path = DEFAULT_ROOT_PATH;
		} else {
			if (path.startsWith("/~/")) {
				this.path = path.substring(3);
			} else {
				this.path = path;
			}
		}
	}

	@Override
	public FileSystem getFileSystem() {
		return this.host;
	}

	@Override
	public Path getRoot() {
		if (path.equals(DEFAULT_ROOT_PATH)) {
			return this;
		}
		return new WebdavPath(this.host, DEFAULT_ROOT_PATH);
	}

	@Override
	public boolean isAbsolute() {
		return path.startsWith(PATH_SEP);
	}

	public String getPathString() {
		return this.path;
	}
	
	@Override
	public int compareTo(Path arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean endsWith(Path arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean endsWith(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Path getFileName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Path getName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNameCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Path getParent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<Path> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path normalize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WatchKey register(WatchService arg0, Kind<?>... arg1)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WatchKey register(WatchService arg0, Kind<?>[] arg1,
			Modifier... arg2) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path relativize(Path arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path resolve(Path arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path resolve(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path resolveSibling(Path arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path resolveSibling(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean startsWith(Path arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startsWith(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Path subpath(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path toAbsolutePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File toFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path toRealPath(LinkOption... arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI toUri() {
		// TODO Auto-generated method stub
		return null;
	}

}
