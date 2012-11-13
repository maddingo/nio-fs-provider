package no.uis.nio.webdav;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
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
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.util.SardineException;

public class WebdavFileSystemProvider extends FileSystemProvider {

  private static final int DEFAULT_PORT = 80;
  private Map<URI, WebdavFileSystem> hosts = new HashMap<URI, WebdavFileSystem>();

  @Override
  public void copy(Path fileFrom, Path fileTo, CopyOption... options) throws IOException {

    if ((fileFrom instanceof WebdavPath) == false) {
      throw new IllegalArgumentException(fileFrom.toString());
    }

    if ((fileTo instanceof WebdavPath) == false) {
      throw new IllegalArgumentException(fileTo.toString());
    }

    WebdavPath wPathTo = (WebdavPath)fileTo;

    WebdavFileSystem webdavHost = (WebdavFileSystem)fileTo.getFileSystem();

    Sardine webdav = webdavHost.getSardine();

    webdav.put(wPathTo.toUri().toString(), Files.readAllBytes(fileFrom));
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
    
    if ((dir instanceof WebdavPath) == false) {
      throw new IllegalArgumentException(dir.toString());
    }

    WebdavPath wDir = (WebdavPath)dir;

    WebdavFileSystem webdavHost = (WebdavFileSystem)dir.getFileSystem();

    Sardine webdav = webdavHost.getSardine();
    
    createDirectoryRecursive(webdav, wDir, attrs);
  }

  private void createDirectoryRecursive(Sardine webdav, WebdavPath wDir, FileAttribute<?>[] attrs) throws IOException {

    if (webdav.exists(wDir.toUri().toString())) {
      return;
    }
    
    WebdavPath parent = (WebdavPath)wDir.getParent();
    if (parent != null) {
      createDirectoryRecursive(webdav, parent, attrs);
    }
    webdav.createDirectory(wDir.toUri().toString());
  }

  @Override
  public void delete(Path dir) throws IOException {
    if ((dir instanceof WebdavPath) == false) {
      throw new IllegalArgumentException(dir.toString());
    }

    WebdavPath wDir = (WebdavPath)dir;
    WebdavFileSystem webdavHost = (WebdavFileSystem)dir.getFileSystem();

    Sardine webdav = webdavHost.getSardine();

    String dirString="";
    try {
      dirString = wDir.toUri().toString();
      webdav.delete(dirString);
    } catch(SardineException se) {
      if (Objects.equals(se.getResponsePhrase(), "Not Found")) {
        throw new NoSuchFileException(dirString);
      }
      if (se.getCause() instanceof IOException) {
        throw (IOException)se.getCause();
      }
      throw new IOException(se);
    }
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    try {
      return getWebdavHost(uri, true);
    } catch(Exception ex) {
      throw new FileSystemNotFoundException(uri.toString());
    }
  }

  @Override
  public Path getPath(URI uri) {
    WebdavFileSystem host;
    try {
      host = getWebdavHost(uri, true);
    } catch(URISyntaxException e) {
      throw new FileSystemNotFoundException(uri.toString());
    }

    if (host != null) {
      return new WebdavPath(host, uri.getPath());
    }
    return null;
  }

  private WebdavFileSystem getWebdavHost(URI uri, boolean create) throws URISyntaxException {
    String host = uri.getHost();
    int port = uri.getPort();
    if (port == -1) {
      port = DEFAULT_PORT;
    }
    String userInfo = uri.getUserInfo();
    URI serverUri = new URI(getScheme(), userInfo, host, port, null, null, null);

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
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
    if (!webdavFs.getSardine().exists(path.toUri().toString())) {
      throw new NoSuchFileException(path.toUri().toString());
    }
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSameFile(Path path, Path path2) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException
  {
    return new SardineChannel((WebdavPath)path);
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path arg0, Filter<? super Path> arg1) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    try {
      return getWebdavHost(uri, true);
    } catch(URISyntaxException e) {
      throw new FileSystemException(e.toString());
    }
  }

  @Override
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
    WebdavFileSystem wfs = (WebdavFileSystem)path.getFileSystem();
    List<DavResource> resources = wfs.getSardine().getResources(path.toUri().toString());
    if (resources.size() != 1) {
      throw new IllegalArgumentException();
    }
    final DavResource res = resources.get(0);
    
    BasicFileAttributes attrs = new BasicFileAttributes() {
      
      @Override
      public long size() {
        return -1;
      }
      
      @Override
      public FileTime lastModifiedTime() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public FileTime lastAccessTime() {
        return FileTime.fromMillis(System.currentTimeMillis());
      }
      
      @Override
      public boolean isSymbolicLink() {
        return false;
      }
      
      @Override
      public boolean isRegularFile() {
        return !res.isDirectory();
      }
      
      @Override
      public boolean isOther() {
        return false;
      }
      
      @Override
      public boolean isDirectory() {
        return res.isDirectory();
      }
      
      @Override
      public Object fileKey() {
        return null;
      }
      
      @Override
      public FileTime creationTime() {
        return FileTime.fromMillis(res.getCreation().getTime());
      }
    };
    
    return (A)attrs;
  }

  @Override
  public Map<String, Object> readAttributes(Path arg0, String arg1, LinkOption... arg2) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(Path arg0, String arg1, Object arg2, LinkOption... arg3) throws IOException {
    throw new UnsupportedOperationException();
  }
}
