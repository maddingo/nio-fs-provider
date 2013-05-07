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

package no.uis.nio.smb;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import jcifs.smb.SmbFile;

/**
 * Denotes a path in an SMB share.
 */
public class SMBPath extends SMBBasePath {

  private static final int MIN_COMMON_LEVEL = 4;
  private static final String REGEX_BACKSLASH = "\\\\";
  private final SmbFile file;
  private final SMBShare fileSystem;
  private final SMBFileSystemProvider provider;
  private final URI uri;
  
  public SMBPath(SMBFileSystemProvider provider, URI uri) throws IOException, URISyntaxException {
    super(toPublicString(uri));
    SmbFile f = new SmbFile(new URL(null, uri.toString(), new jcifs.smb.Handler()));
    if (f.getShare() == null) {
      throw new IllegalArgumentException(uri.toString());
    }
    this.file = f;
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
    } catch(IOException | URISyntaxException e) {
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

    String[] thisParts = thisUNC.split(REGEX_BACKSLASH);
    String[] otherParts = otherUNC.split(REGEX_BACKSLASH);

    // find common root
    int commonLevel = 0;
    for (; commonLevel < Math.min(thisParts.length, otherParts.length); commonLevel++) {
      if (!otherParts[commonLevel].equals(thisParts[commonLevel])) {
        break;
      }
    }
    
    if (commonLevel < MIN_COMMON_LEVEL) {
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
    } catch(IOException | URISyntaxException e) {
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

  private static String toPublicString(URI uri) throws URISyntaxException {
    return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
  }
}

