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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Denotes a WebDAV Path.  
 */
public class WebdavPath implements Path {

  private static final String NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH = "Need to be an instance of WebdavPath";
  private static final String PARENT_PATH = "..";
  private static final String PATH_SEP = "/";
  private static final String DEFAULT_ROOT_PATH = PATH_SEP;
  private final String path;
  private final WebdavFileSystem host;

  public WebdavPath(WebdavFileSystem webdavHost, String path) {
    this.host = webdavHost;
    if (path == null) {
      this.path = DEFAULT_ROOT_PATH;
    } else {
      String p = path.trim();
      if (!p.startsWith(PATH_SEP)) {
        this.path = PATH_SEP + p;
      } else {
        this.path = p;
      }
    }
  }

  @Override
  public FileSystem getFileSystem() {
    return this.host;
  }

  @Override
  public Path getRoot() {
    if (path.equals(DEFAULT_ROOT_PATH)) {
      return this;
    }
    return new WebdavPath(this.host, DEFAULT_ROOT_PATH);
  }

  @Override
  public boolean isAbsolute() {
    return path.length() > 0 && path.startsWith(PATH_SEP);
  }

  /**
   * @deprecated will be removed in future releases
   */
  @Deprecated
  public String getPathString() {
    return this.path;
  }

  @Override
  public int compareTo(Path other) {
    // TODO Auto-generated method stub
    return 0;
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
  public Path getFileName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path getName(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getNameCount() {
    return 0;
  }

  @Override
  public Path getParent() {
    if (path.equals(DEFAULT_ROOT_PATH)) {
      return null;
    }
    String p1 = this.path;
    if (p1.endsWith(PATH_SEP)) {
      p1 = p1.substring(0, p1.length() - 1);
    }
    int lastSep = p1.lastIndexOf(PATH_SEP);
    if (lastSep > 0) {
      String parentString = p1.substring(0, lastSep + 1);
      return new WebdavPath(this.host, parentString);
    }
    return null;
  }

  @Override
  public Iterator<Path> iterator() {
    List<Path> plist = new LinkedList<Path>();

    for (Path p = this; p != null; p = p.getParent()) {
      plist.add(0, p);
    }
    return plist.iterator();
  }

  @Override
  public Path normalize() {
    if (!(this instanceof WebdavPath)) {
      throw new IllegalArgumentException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);
    }
    String other = this.path;
    if (other.contains(PARENT_PATH)) {
      if ("".equals(this.path)) {
        return new WebdavPath(this.host, this.path);
      }
      int leadingSlashes = 0;
      for (; leadingSlashes < this.path.length() && this.path.charAt(leadingSlashes) == '/';) {
        leadingSlashes++;
      }
      boolean isDir = this.path.charAt(this.path.length() - 1) == '/';
      StringTokenizer st = new StringTokenizer(this.path, PATH_SEP);
      Deque<String> clean = new LinkedList<String>();
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (PARENT_PATH.equals(token)) {
          if (!clean.isEmpty() && !PARENT_PATH.equals(clean.getLast())) {
            clean.removeLast();
            if (!st.hasMoreTokens()) {
              isDir = true;
            }
          } else {
            clean.add(PARENT_PATH);
          }
        } else if (!".".equals(token) && !"".equals(token)) {
          clean.add(token);
        }
      }
      StringBuffer sb = new StringBuffer();
      while (leadingSlashes-- > 0) {
        sb.append(PATH_SEP);
      }
      for (Iterator<?> it = clean.iterator(); it.hasNext();) {
        sb.append(it.next());
        if (it.hasNext()) {
          sb.append('/');
        }
      }
      if (isDir && sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
        sb.append('/');
      }
      return new WebdavPath(this.host, sb.toString());
    } else {
      return this;
    }
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... arg2) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path relativize(Path other) {
    if (!(other instanceof WebdavPath)) {
      throw new IllegalArgumentException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);
    }

    if (!other.getFileSystem().equals(this.getFileSystem())) {
      throw new IllegalArgumentException("Wrong File System Type");
    }

    Path base = this;
    WebdavPath current = (WebdavPath)other;

    String[] bParts = this.path.split(PATH_SEP);
    String[] cParts = current.path.split(PATH_SEP);

    if (bParts.length > 0 && !base.toString().endsWith(PATH_SEP)) {
      bParts = Arrays.copyOf(bParts, bParts.length - 1);
    }

    int i = 0;
    while (i < bParts.length && i < cParts.length && bParts[i].equals(cParts[i])) {
      i++;
    }

    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < (bParts.length - i); j++) {
      sb.append(PATH_SEP);
    }
    for (int j = i; j < cParts.length; j++) {
      if (j != i) {
        sb.append(PATH_SEP);
      }
      sb.append(cParts[j]);
    }
    return new WebdavPath(this.host, sb.toString());
  }

  @Override
  public Path resolve(Path other) {
    if (other.isAbsolute()) {
      return other;
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Path resolve(String other) {
    if (other.startsWith(PATH_SEP)) {
      throw new IllegalArgumentException(other);
    }
    StringBuilder resolvedPath = new StringBuilder(this.path);
    if (!this.path.endsWith(PATH_SEP)) {
      resolvedPath.append(PATH_SEP);
    }
    resolvedPath.append(other);
    return new WebdavPath(this.host, resolvedPath.toString());
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
  public Path subpath(int beginIndex, int endindex) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Path toAbsolutePath() {
    return this;
  }

  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path toRealPath(LinkOption... options) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI toUri() {
    String scheme = (host.provider() instanceof WebdavsFileSystemProvider) ? "https" : "http";
    String server = host.getHost();
    int port = host.getPort();

    URI sardineUri;
    try {
      sardineUri = new URI(scheme, null, server, port, path, null, null);
      return sardineUri;
    } catch(URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
