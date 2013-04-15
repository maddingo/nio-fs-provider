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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import jcifs.smb.SmbFileOutputStream;

public class SMBByteChannel implements SeekableByteChannel {

  private final SMBPath path;
  private boolean closed=true;
  private SmbFileOutputStream out;

  public SMBByteChannel(SMBPath smbPath) throws IOException {
    this.path = smbPath;
    out = new SmbFileOutputStream(path.getSmbFile());
  }

  @Override
  public boolean isOpen() {
    return out.isOpen();
  }

  @Override
  public void close() throws IOException {
    out.close();
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    int len = src.remaining();
    byte[] buf = new byte[len];
    while (src.hasRemaining()) {
      src.get(buf);
      out.write(buf);
    }
    return len;
  }

  @Override
  public long position() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public SeekableByteChannel position(long newPosition) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long size() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public SeekableByteChannel truncate(long size) throws IOException {
    throw new UnsupportedOperationException();
  }
}
