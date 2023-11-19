package no.maddin.niofs.smb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Implementation of {@link SeekableByteChannel} for writing SMB files.
 * 
 * Reading is not supported at the moment.
 */
public class SMBByteChannel implements SeekableByteChannel {

  private final SMBPath path;

  SMBByteChannel(SMBPath smbPath) throws IOException {
    this.path = smbPath;
  }

  @Override
  public boolean isOpen() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    throw new UnsupportedOperationException();
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
