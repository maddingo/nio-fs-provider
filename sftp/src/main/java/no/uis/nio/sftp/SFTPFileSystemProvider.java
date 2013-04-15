/*
 Copyright 2012-2013 University of Stavanger, Norway

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package no.uis.nio.sftp;

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
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

public class SFTPFileSystemProvider extends FileSystemProvider {
  private static final int DEFAULT_PORT = 22;
  private Map<URI, SFTPHost> hosts = new HashMap<URI, SFTPHost>();

  private JSch jsch = new JSch();

  public SFTPFileSystemProvider() {
    JSch.setConfig("StrictHostKeyChecking", "no");
  }

  @Override
  public String getScheme() {
    return "sftp";
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    try {
      return getSFTPHost(uri, true);
    } catch(URISyntaxException e) {
      throw new FileSystemException(e.toString());
    }
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    try {
      return getSFTPHost(uri, true);
    } catch(Exception ex) {
      throw new FileSystemNotFoundException(uri.toString());
    }
  }

  @Override
  public Path getPath(URI uri) {
    SFTPHost host;
    try {
      host = getSFTPHost(uri, true);
    } catch(URISyntaxException e) {
      throw new FileSystemNotFoundException(uri.toString());
    }

    if (host != null) {
      return new SFTPPath(host, uri.getPath());
    }
    return null;
  }

  /**
   * Get a SFTP Host with the given host, user, password and port
   * 
   * @param uri
   * @param create
   *        if {@code true} a new SFTPHost is created if none is registered.
   * @return
   * @throws URISyntaxException
   * @throws FileSystemNotFoundException
   */
  private SFTPHost getSFTPHost(URI uri, boolean create) throws URISyntaxException {
    String host = uri.getHost();
    int port = uri.getPort();
    if (port == -1) {
      port = DEFAULT_PORT;
    }
    String userInfo = uri.getUserInfo();
    URI serverUri = new URI(getScheme(), userInfo, host, port, null, null, null);

    synchronized (hosts) {
      SFTPHost fs = hosts.get(serverUri);
      if (fs == null && create) {
        fs = new SFTPHost(this, serverUri);
        hosts.put(serverUri, fs);
      }
      return fs;
    }
  }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
    if ((dir instanceof SFTPPath) == false) {
      throw new IllegalArgumentException(dir.toString());
    }

    SFTPHost sftpHost = (SFTPHost)dir.getFileSystem();

    String username = sftpHost.getUserName();
    String host = sftpHost.getHost();
    int port = sftpHost.getPort();
    Session session;
    try {
      session = jsch.getSession(username, host, port);
      UserInfo userinfo = new SFTPUserInfo(sftpHost.getPassword());
      session.setUserInfo(userinfo);
      session.connect();
      
      ChannelSftp sftp = (ChannelSftp)session.openChannel("sftp");
      
      sftp.connect();
      
      SFTPPath sftpPath = (SFTPPath)dir; 
      String dirString = sftpPath.getPathString();
      try {
        sftp.mkdir(dirString);
      } catch(SftpException e) {
        throw new IOException(dirString, e);
      }
      
      sftp.quit();

      session.disconnect();
      //throw new UnsupportedOperationException();
    } catch(JSchException e) {
      throw new FileSystemException(e.getMessage());
    }
  }

  @Override
  public void delete(Path path) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void copy(Path source, Path target, CopyOption... options) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isSameFile(Path path, Path path2) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
    // TODO Auto-generated method stub

  }
}
