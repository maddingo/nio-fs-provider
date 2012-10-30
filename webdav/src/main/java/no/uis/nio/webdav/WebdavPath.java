package no.uis.nio.webdav;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WebdavPath implements Path {

  private static final String DEFAULT_ROOT_PATH = "/";
  private static final String PATH_SEP = "/";
  private final String path;
  private final WebdavFileSystem host;

  public WebdavPath(WebdavFileSystem webdavHost, String path) {
    this.host = webdavHost;
    if (path == null) {
      this.path = DEFAULT_ROOT_PATH;
    } else {
      path = path.trim();
      if (!path.startsWith(PATH_SEP)) {
        this.path = PATH_SEP + path;
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
    return true;
  }

  @Deprecated
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
    return 0;
  }

  @Override
  public Path getParent() {
    if (path.equals(DEFAULT_ROOT_PATH)) {
      return null;
    }
    String p1 = this.path;
    if (p1.endsWith(PATH_SEP)) {
      p1 = p1.substring(0, p1.length()-1);
    }
    int lastSep = p1.lastIndexOf(PATH_SEP);
    if (lastSep > 0) {
      String parentString = p1.substring(0, lastSep+1);
      return new WebdavPath(this.host, parentString);
    }
    return null;
  }

  @Override
  public Iterator<Path> iterator() {
    List<Path> plist = new LinkedList<Path>();
    
    for (Path p = this; p != null; p = p.getParent()) {
      plist.add(0, p);
    }
    return plist.iterator();
  }

  @Override
  public Path normalize() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WatchKey register(WatchService arg0, Kind<?>... arg1) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WatchKey register(WatchService arg0, Kind<?>[] arg1, Modifier... arg2) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path relativize(Path other) {
    // TODO we don't support relative paths yet
    return other;
  }

  @Override
  public Path resolve(Path other) {
    if (other.isAbsolute()) {
      return other;
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Path resolve(String part) {
    return new WebdavPath(this.host, this.path + PATH_SEP + part);
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
    return this;
  }

  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path toRealPath(LinkOption... arg0) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI toUri() {
    String scheme = (host.provider() instanceof WebdavsFileSystemProvider) ? "https" : "http";
    String server = host.getHost();
    int port = host.getPort();

    URI sardineUri;
    try {
      sardineUri = new URI(scheme, null, server, port, path, null, null);
      return sardineUri;
    } catch(URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
