package no.uis.nio.webdav;

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
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.util.SardineException;

public class WebdavFileSystemProvider extends FileSystemProvider {

  private static final int DEFAULT_PORT = 80;
  private Map<URI, WebdavFileSystem> hosts = new HashMap<URI, WebdavFileSystem>();

  @Override
  public void copy(Path fileFrom, Path fileTo, CopyOption... arg2) throws IOException {

    if ((fileFrom instanceof WebdavPath) == false) {
      throw new IllegalArgumentException(fileFrom.toString());
    }

    if ((fileTo instanceof WebdavPath) == false) {
      throw new IllegalArgumentException(fileTo.toString());
    }

    WebdavPath wPathTo = (WebdavPath)fileTo;

    WebdavFileSystem webdavHost = (WebdavFileSystem)fileTo.getFileSystem();

    Sardine webdav = webdavHost.getSardine();

    try {
      webdav.put(wPathTo.getSardineUri().toString(), Files.readAllBytes(fileFrom));
    } catch(URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... arg1) throws IOException {
    
    if ((dir instanceof WebdavPath) == false) {
      throw new IllegalArgumentException(dir.toString());
    }

    WebdavPath wDir = (WebdavPath)dir;

    WebdavFileSystem webdavHost = (WebdavFileSystem)dir.getFileSystem();

    Sardine webdav = webdavHost.getSardine();
    
    try {
      webdav.createDirectory(wDir.getSardineUri().toString());
    } catch(URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
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
      dirString = wDir.getSardineUri().toString();
      webdav.delete(dirString);
    } catch(URISyntaxException e) {
      throw new IllegalArgumentException(e);
    } catch(SardineException se) {
      if (se.getResponsePhrase().equals("Not Found")) {
        throw new NoSuchFileException(dirString);
      }
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
  public <V extends FileAttributeView> V getFileAttributeView(Path arg0, Class<V> arg1, LinkOption... arg2) {
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
  public void move(Path arg0, Path arg1, CopyOption... arg2) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public SeekableByteChannel newByteChannel(Path arg0, Set<? extends OpenOption> arg1, FileAttribute<?>... arg2)
      throws IOException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path arg0, Filter<? super Path> arg1) throws IOException {
    // TODO Auto-generated method stub
    return null;
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
  public <A extends BasicFileAttributes> A readAttributes(Path arg0, Class<A> arg1, LinkOption... arg2) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Object> readAttributes(Path arg0, String arg1, LinkOption... arg2) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setAttribute(Path arg0, String arg1, Object arg2, LinkOption... arg3) throws IOException {
    // TODO Auto-generated method stub

  }
}
