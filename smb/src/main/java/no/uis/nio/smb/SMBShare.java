package no.uis.nio.smb;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.security.Principal;
import java.util.Set;

public class SMBShare extends FileSystem {

  private final Principal principal;
  private final String share;
  private final String server;
  private final SMBFileSystemProvider provider;

  public SMBShare(SMBFileSystemProvider provider, String server, String share, Principal principal) {
    this.server = server;
    this.share = share;
    this.principal = principal;
    this.provider = provider;
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
  public String getSeparator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
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
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WatchService newWatchService() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
}
