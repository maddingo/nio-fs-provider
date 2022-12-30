package no.maddin.niofs.smb;

import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Denotes a Windows share.
 */
public class SMBShare extends FileSystem {

  private final SMBFileSystemProvider provider;

  private final DiskShare diskShare;

  SMBShare(SMBFileSystemProvider provider, String server, String share, UsernamePassword usernamePassword) throws IOException {
    this.provider = provider;

    Connection connection = provider.getClient().connect(server);
    AuthenticationContext auth = new AuthenticationContext(usernamePassword.username, usernamePassword.password.toCharArray(), usernamePassword.domain);
    Session session = connection.authenticate(auth);

    // Connect to Share
    diskShare = (DiskShare) session.connectShare(share);
  }
  
  @Override
  public FileSystemProvider provider() {
    return provider;
  }

  @Override
  public void close() throws IOException {
    diskShare.close();
  }

  @Override
  public boolean isOpen() {
    return Optional.ofNullable(diskShare)
        .map(Share::isConnected)
        .orElse(Boolean.FALSE);
  }

  @Override
  public boolean isReadOnly() {
    // TODO find out how to check this
    return true;
  }

  @Override
  public String getSeparator() {
    return "/";
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    return List.of();
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
    // TODO handle "more"
    return new SMBPath(this, first);
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

  public DiskShare getDiskShare() {
    return diskShare;
  }

  URI toUri() {
    try {
      String share = Optional.ofNullable(diskShare.getSmbPath().getShareName())
          .map(s -> {
            if (s.startsWith("/")) {
              return s;
            } else {
              return "/" + s;
            }
          })
          .orElse("/");
      return new URI("smb", diskShare.getSmbPath().getHostname(), share, null);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public record UsernamePassword(String username, String password, String domain){}
}
