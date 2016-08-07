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

package no.maddin.niofs.smb;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jcifs.smb.SmbFile;

/**
 * Common super-class for {@link SMBFileSystemProvider} and {@link SMBPath}.
 */
public class SMBBasePath implements Path {

  private final String path;
  
  public SMBBasePath(String path) {
    this.path = path;
  }
  
  @Override
  public FileSystem getFileSystem() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAbsolute() {
    return false;
  }

  @Override
  public Path getRoot() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path getFileName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path getParent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getNameCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path getName(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path subpath(int beginIndex, int endIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean startsWith(Path other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean startsWith(String other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean endsWith(Path other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean endsWith(String other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path normalize() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path resolve(Path other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path resolve(String other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path resolveSibling(Path other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path resolveSibling(String other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path relativize(Path other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public URI toUri() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path toAbsolutePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path toRealPath(LinkOption... options) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * Treat successive parent directories, e.g. <code>..\..\..\</code>, as one path.
   */
  @Override
  public Iterator<Path> iterator() {
    String[] parts = path.split("\\\\");
    
    List<Path> list = new ArrayList<Path>(parts.length);
    boolean isDirectory = path.endsWith("\\");
    StringBuilder sb = null;
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if ("..".equals(part)) {
        if (sb == null) {
          sb = new StringBuilder();
        }
        sb.append("..\\");
      } else {
        if (sb != null) {
          list.add(new SMBBasePath(sb.toString()));
          sb = null;
        }
        StringBuilder partSB = new StringBuilder(part);
        if (i < (parts.length - 1) || isDirectory) {
          partSB.append('\\');
        }
        list.add(new SMBBasePath(partSB.toString()));
      }
    }
    
    return list.iterator();
  }

  @Override
  public int compareTo(Path other) {
    throw new UnsupportedOperationException();
  }

  public SMBFileAttributes getAttributes() throws IOException {
    throw new UnsupportedOperationException();
  }

  public SmbFile getSmbFile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return path;
  }
}
