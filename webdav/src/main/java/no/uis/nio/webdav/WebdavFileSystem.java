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

package no.uis.nio.webdav;

import java.io.IOException;
import java.net.URI;
import java.net.ProxySelector;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

/**
 * WebDAV implementation of a FileSystem.
 */
public class WebdavFileSystem extends FileSystem {

  private final FileSystemProvider provider;
  private final URI serverUri;
  private final int port;
  private final String host;
  private final String password;
  private final String username;

  public WebdavFileSystem(WebdavFileSystemProvider provider, URI serverUri) {
    this.provider = provider;
    this.serverUri = serverUri;
    this.host = serverUri.getHost();
    this.port = serverUri.getPort();
    String[] ui = serverUri.getUserInfo().split(":");
    this.username = ui[0];
    this.password = ui[1];
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
  public Iterable<FileStore> getFileStores() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path getPath(String first, String... more) {
      String path;
      if (more.length == 0) {
          path = first;
      } else {
          StringBuilder sb = new StringBuilder();
          sb.append(first);
          for (String segment: more) {
              if (segment.length() > 0) {
                  if (sb.length() > 0)
                      sb.append(getSeparator());
                  sb.append(segment);
              }
          }
          path = sb.toString();
      }
      return new WebdavPath(this, path);
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getSeparator() {
    return "/";
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    // TODO Auto-generated method stub
    return null;
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
  public WatchService newWatchService() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Check if one filesystem is equal to another. Checks Host, username and Port.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof WebdavFileSystem)) {
      throw new IllegalArgumentException("Argument must be of instance WebdavFileSystem");
    }
    WebdavFileSystem current = (WebdavFileSystem)other;

    return current.host.equals(this.host) && current.username.equals(this.username) && current.port == this.port;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public String getUserName() {
    return this.username;
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  public String getPassword() {
    return this.password;
  }

  protected Sardine getSardine() throws IOException {
      return SardineFactory.begin(username, password, ProxySelector.getDefault());
  }
}
