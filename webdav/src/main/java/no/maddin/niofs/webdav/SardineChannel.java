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

package no.maddin.niofs.webdav;

import com.github.sardine.Sardine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;

/**
 * A {@link SeekableByteChannel} based on Sardine.
 */
public class SardineChannel implements SeekableByteChannel {

  private final Sardine sardine;
  private final WebdavPath path;
  private InputStream in;
  private ByteArrayOutputStream out;
  private int position = 0;

  public SardineChannel(WebdavPath webdavPath, Set<? extends OpenOption> options) throws IOException {
    this.sardine = ((WebdavFileSystem)webdavPath.getFileSystem()).getSardine();
    this.path = webdavPath;
    if (isWritable(options)) {
      getOutputStream();
    }
  }

  private static boolean isWritable(Set<? extends OpenOption> options) {
    return options != null && options.contains(StandardOpenOption.WRITE);
  }

  @Override
  public boolean isOpen() {
    return in != null || out != null;
  }

  @Override
  public void close() throws IOException {
    synchronized (sardine) {
      if (in != null) {
        in.close();
        in = null;
      }
      if (out != null) {

        sardine.put(path.toUri().toString(), out.toByteArray(), "application/octet-stream");

        out.close();
        out = null;
      }
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    synchronized (sardine) {
      if (in == null) {
        in = sardine.get(path.toUri().toString());
      }
      if (dst.hasArray()) {
    	int numBytesToRead = in.read(dst.array());
    	position += numBytesToRead;
		return numBytesToRead;
      }
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public int write(ByteBuffer src) throws IOException {

    OutputStream os = getOutputStream();
    int len = src.remaining();
    byte[] buf = new byte[len];
    while (src.hasRemaining()) {
      src.get(buf);
      os.write(buf);
    }
    
    position += len;
    return len;
  }

  private ByteArrayOutputStream getOutputStream() {
    ByteArrayOutputStream os = out;
    if (os == null) {
      synchronized (sardine) {
        os = out;
        if (os == null) {
          os = new ByteArrayOutputStream();
          this.out = os;
        }
      }
    }
    return os;
  }

  @Override
  public long position() {
	  return position;
  }

  @Override
  public SeekableByteChannel position(long newPosition) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long size() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public SeekableByteChannel truncate(long size) {
    throw new UnsupportedOperationException();
  }
}
