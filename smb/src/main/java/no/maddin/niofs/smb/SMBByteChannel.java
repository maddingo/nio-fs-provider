package no.maddin.niofs.smb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import jcifs.smb.SmbFileOutputStream;

/**
 * Implementation of {@link SeekableByteChannel} for writing SMB files.
 * 
 * Reading is not supported at the moment.
 */
public class SMBByteChannel implements SeekableByteChannel {

  private final SMBPath path;
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
