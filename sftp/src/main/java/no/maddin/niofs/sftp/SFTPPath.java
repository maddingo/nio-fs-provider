package no.maddin.niofs.sftp;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Iterator;

/**
 * A Path implementation for SFTP.
 */
public class SFTPPath implements Path {

  private static final String HOME_PREFIX = "/~/";
  private static final int HOME_PREFIX_LEN = HOME_PREFIX.length();
  private static final String DEFAULT_ROOT_PATH = "";
  private static final String PATH_SEP = "/";
  private final String path;
  private final SFTPHost host;

  public SFTPPath(SFTPHost sftpHost, String path) {
    this.host = sftpHost;
    
    // TODO split the path in ist components
    if (path == null || path.trim().isEmpty()) {
      this.path = DEFAULT_ROOT_PATH;
    } else {
      if (path.startsWith(HOME_PREFIX)) {
        this.path = path.substring(HOME_PREFIX_LEN);
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
  public boolean isAbsolute() {
    return path.startsWith(PATH_SEP);
  }

  @Override
  public Path getRoot() {
    if (path.equals(DEFAULT_ROOT_PATH)) {
      return this;
    }
    return new SFTPPath(this.host, DEFAULT_ROOT_PATH);
  }

  @Override
  public Path getFileName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path getParent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getNameCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Path getName(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path subpath(int beginIndex, int endIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean startsWith(Path other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean startsWith(String other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean endsWith(Path other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean endsWith(String other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Path normalize() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path resolve(Path other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path resolve(String other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path resolveSibling(Path other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path resolveSibling(String other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path relativize(Path other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI toUri() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path toAbsolutePath() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path toRealPath(LinkOption... options) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public File toFile() {
    
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterator<Path> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int compareTo(Path other) {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getPathString() {
    return this.path;
  }
}