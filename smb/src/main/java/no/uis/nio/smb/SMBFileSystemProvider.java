package no.uis.nio.smb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jcifs.Config;
import jcifs.smb.NtStatus;
import jcifs.smb.SmbException;

public class SMBFileSystemProvider extends FileSystemProvider {

  /**
   * Provider based on jcifs. 
   * jCifs can be configured with <code>/jcifs-config.properties</code>, if jCifs properties are not already loaded.
   * If it was previously configured, a warning is issued. 
   * @throws IOException
   */
  public SMBFileSystemProvider() throws IOException {
    
    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    Config.list(new PrintStream(outBuffer, true));
    if (outBuffer.size() > 0) {
      Logger.getLogger(getClass().getName()).warning("jcifs already configured with:\n"+outBuffer.toString());
    }
    InputStream config = getClass().getResourceAsStream("/jcifs-config.properties");
    if (config != null) {
      Config.load(config);
    }
  }
  
  @Override
  public String getScheme() {
    return "smb";
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path getPath(URI uri) {
    try {
      return new SMBPath(this, uri);
    } catch(IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException
  {
    SMBPath smbPath = toSMBPath(path);
    return new SMBByteChannel(smbPath); 
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
    SMBBasePath smbFile = toSMBPath(dir);
    if (!smbFile.isAbsolute()) {
      throw new IllegalArgumentException(dir.toString());
    }
    
    SMBDirectoryStream dirStream = new SMBDirectoryStream(this, (SMBPath)smbFile, filter);
    return dirStream;
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
    try {
      toSMBPath(dir).getSmbFile().mkdirs();
    } catch(SmbException e) {
      if (e.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_COLLISION) {
        throw new FileAlreadyExistsException(dir.toString(), null, e.getMessage());
      }
    }
  }

  @Override
  public void delete(Path path) throws IOException {
    SMBPath smbPath = toSMBPath(path);
    smbPath.getSmbFile().delete();
  }

  @Override
  public void copy(Path source, Path target, CopyOption... options) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSameFile(Path path, Path path2) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    
    if (!toSMBPath(path).getSmbFile().exists()) {
      throw new NoSuchFileException(path.toString());
    }
  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
    if (type == BasicFileAttributes.class || type == SMBFileAttributes.class) {
      return (A)toSMBPath(path).getAttributes();
    }
    return null;
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
    throw new UnsupportedOperationException();
  }

  public static <A extends SMBBasePath> A toSMBPath(Path path) {
    if (path == null) {
      throw new NullPointerException();
    }
    if (!(path instanceof SMBBasePath)) {
      throw new ProviderMismatchException();
    }
    return (A)path;

  }
}
