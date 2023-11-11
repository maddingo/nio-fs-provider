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
import java.util.*;

/**
 * Denotes a Windows share.
 */
public class SMBShare extends FileSystem {

  public static final String SMBFS_SEPARATOR = "/";
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
    throw new UnsupportedOperationException();
  }

  @Override
  public String getSeparator() {
    return SMBFS_SEPARATOR;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    return Collections.singletonList(new SMBPath(this, getSeparator()));
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path getPath(String first, String... more) {
    // TODO handle "more"
    return new SMBPath(this, first);
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchService newWatchService() {
    throw new UnsupportedOperationException();
  }

  public DiskShare getDiskShare() {
    return diskShare;
  }

  URI toUri() {
    try {
      String share = Optional.ofNullable(diskShare.getSmbPath().getShareName())
          .map(s -> {
            if (s.startsWith(getSeparator())) {
              return s;
            } else {
              return getSeparator() + s;
            }
          })
          .orElse(getSeparator());
      return new URI("smb", diskShare.getSmbPath().getHostname(), share, null);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SMBShare smbShare = (SMBShare) o;
    return Objects.equals(provider, smbShare.provider) && Objects.equals(diskShare, smbShare.diskShare);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, diskShare);
  }

  @Override
  public String toString() {
    return "smb:" + diskShare.getSmbPath().toString().replace('\\', '/');
  }

  @SuppressWarnings("unused")
  public static class UsernamePassword {
    private final String username;
    private final String password;
    private final String domain;

    public UsernamePassword(String username, String password, String domain) {
      this.username = username;
      this.password = password;
      this.domain = domain;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

    public String getDomain() {
      return domain;
    }
  }
}
