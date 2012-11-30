package no.uis.nio.smb;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import jcifs.smb.SmbFile;

public class SMBPath extends SMBBasePath {

  private final SmbFile file;
  private final SMBShare fileSystem;
  private final SMBFileSystemProvider provider;
  private final URI uri;
  
  public SMBPath(SMBFileSystemProvider provider, URI uri) throws IOException {
    super(uri.toString());
    SmbFile _file = new SmbFile(new URL(null, uri.toString(), new jcifs.smb.Handler()));
    if (_file.getShare() == null) {
      throw new IllegalArgumentException(uri.toString());
    }
    this.file = _file;
    this.provider = provider;
    this.uri = uri;
    this.fileSystem = new SMBShare(provider, file.getServer(), file.getShare(), file.getPrincipal());
  }

  @Override
  public FileSystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public boolean isAbsolute() {
    return true;
  }

  @Override
  public Path getParent() {
    String parent = file.getParent();
    try {
      URI parentUri = new URI(parent);
      return new SMBPath(provider, parentUri);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Path toAbsolutePath() {
    return this;
  }

  @Override
  public File toFile() {
    return new File(file.getUncPath());
  }

  @Override
  public URI toUri() {
    return this.uri;
  }

  @Override
  public Path relativize(Path other) {
    SMBBasePath otherSmbPath = SMBFileSystemProvider.toSMBPath(other);
    if (!other.isAbsolute()) {
      throw new IllegalArgumentException(other.toString());
    }
   
    String thisUNC = file.getUncPath();
    String otherUNC = otherSmbPath.getSmbFile().getUncPath();

    String[] thisParts = thisUNC.split("\\\\");
    String[] otherParts = otherUNC.split("\\\\");

    // find common root
    int commonLevel = 0;
    for (; commonLevel < Math.min(thisParts.length, otherParts.length); commonLevel++) {
      if (!otherParts[commonLevel].equals(thisParts[commonLevel])) {
        break;
      }
    }
    
    if (commonLevel < 4) {
      // the share and server are not equal
      throw new IllegalArgumentException(other.toString());
    }
    
    StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < (thisParts.length - commonLevel); i++) {
      sb.append("..\\");
    }
    for (int i = commonLevel; i < otherParts.length; i++) {
      sb.append(otherParts[i]);
      sb.append("\\");
    }
    
    return new SMBBasePath(sb.toString());
  }

  @Override
  public Path resolve(String other) {
    URI otherUri = this.uri.resolve(other);
    try {
      return new SMBPath(provider, otherUri);
    } catch(IOException e) {
      throw new IllegalArgumentException(other, e);
    }
  }

  @Override
  public Path resolve(Path other) {
    SMBBasePath otherPath = SMBFileSystemProvider.toSMBPath(other);
    if (otherPath.isAbsolute()) {
      throw new IllegalArgumentException();
    }
    return resolve(otherPath.toString());
  }

  @Override
  public SmbFile getSmbFile() {
    return file;
  }
  
  @Override
  public SMBFileAttributes getAttributes() throws IOException {
    return new SMBFileAttributes(file.getUncPath(), file.getAttributes());
  }
}
