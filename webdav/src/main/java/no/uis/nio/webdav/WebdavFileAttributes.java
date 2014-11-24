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

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import com.github.sardine.DavResource;

/**
 * File attributes for WebDAV.
 */
public class WebdavFileAttributes implements BasicFileAttributes {
  private final DavResource res;

  WebdavFileAttributes(DavResource res) {
    this.res = res;
  }

  @Override
  public long size() {
    return -1;
  }

  @Override
  public FileTime lastModifiedTime() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileTime lastAccessTime() {
    return FileTime.fromMillis(System.currentTimeMillis());
  }

  @Override
  public boolean isSymbolicLink() {
    return false;
  }

  @Override
  public boolean isRegularFile() {
    return !res.isDirectory();
  }

  @Override
  public boolean isOther() {
    return false;
  }

  @Override
  public boolean isDirectory() {
    return res.isDirectory();
  }

  @Override
  public Object fileKey() {
    return null;
  }

  @Override
  public FileTime creationTime() {
    return FileTime.fromMillis(res.getCreation().getTime());
  }
}
