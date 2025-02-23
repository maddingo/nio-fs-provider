package no.maddin.niofs.sftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JSchByteChannel implements SeekableByteChannel {

    private final @NotNull SFTPFileSystemProvider.SFTPSession sftpSession;
    private final @NotNull String fileName;
    private final Object ioLock = new Object();
    private final InputStream inputStream;
    private final List<FileAttribute<?>> attrs;
    private long ioPosition = 0;
    private final boolean writing;
    private final boolean reading;
    private final OutputStream outputStream;
    private final Set<? extends OpenOption> options;

    JSchByteChannel(JSch jsch, SFTPPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        this.writing = options.contains(StandardOpenOption.WRITE);
        this.reading = options.contains(StandardOpenOption.READ);
        this.attrs = attrs != null ? Arrays.asList(attrs) : Collections.emptyList();
        if (writing && reading) {
            throw new UnsupportedOperationException("Write and Read at the same time is not supported");
        }
        SFTPHost sftpHost = (SFTPHost) path.getFileSystem();

        try {
            this.sftpSession = new SFTPFileSystemProvider.SFTPSession(sftpHost, jsch);
            this.fileName = path.toUri().getPath();
            if (reading) {
                this.inputStream = sftpSession.sftp.get(this.fileName);
                this.outputStream = null;
            } else if (writing) {
                this.outputStream = sftpSession.sftp.put(this.fileName);
                this.inputStream = null;
            } else {
                this.inputStream = null;
                this.outputStream = null;
            }
            this.options = options;
        } catch (SftpException | JSchException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (!reading) {
            throw new UnsupportedOperationException("Channel is not open for reading");
        }
        if (!dst.hasArray()) {
            throw new IllegalArgumentException("ByteBuffer must be backed by an array");
        }
        synchronized(ioLock) {
            int read = inputStream.read(dst.array());
            if (read == -1) {
                inputStream.close();
            } else {
                ioPosition += read;
            }
            return read;
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (!writing) {
            throw new UnsupportedOperationException("Channel is not open for writing");
        }
        synchronized(ioLock) {
            int remaining = src.remaining();
            outputStream.write(src.array(), src.position(), src.remaining());
            ioPosition += remaining;
            return remaining;
        }
    }

    @Override
    public long position() throws IOException {
        return ioPosition;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long size() throws IOException {
        try {
            SftpATTRS stat = sftpSession.sftp.stat(this.fileName);
            return stat.getSize();
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedEncodingException();
    }

    @Override
    public boolean isOpen() {
        return !sftpSession.sftp.isClosed();
    }

    @Override
    public void close() throws IOException {
        synchronized (ioLock) {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
        this.sftpSession.close();
    }
}
