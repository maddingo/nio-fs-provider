package no.maddin.niofs.sftp;

import com.jcraft.jsch.SftpATTRS;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class SFTPFileAttributes implements BasicFileAttributes {
    private final FileTime lastModifiedTime;
    private final FileTime lastAccessTime;
    private final FileTime creationTime;
    private final boolean isRegularFile;
    private final boolean isDirectory;
    private final boolean isSymbolicLink;
    private final boolean isOther;
    private final long size;
    private final Object fileKey;

    SFTPFileAttributes(SftpATTRS stat) {
        this.lastModifiedTime = FileTime.fromMillis(stat.getMTime() * 1000L);
        this.lastAccessTime = FileTime.fromMillis(stat.getATime() * 1000L);
        this.creationTime = FileTime.fromMillis(stat.getMTime() * 1000L);
        this.isRegularFile = stat.isReg();
        this.isDirectory = stat.isDir();
        this.isSymbolicLink = stat.isLink();
        this.isOther = !stat.isReg() && !stat.isDir() && !stat.isLink();
        this.size = stat.getSize();
        this.fileKey = null;
    }

    @Override
    public FileTime lastModifiedTime() {
        return this.lastModifiedTime;
    }

    @Override
    public FileTime lastAccessTime() {
        return this.lastAccessTime;
    }

    @Override
    public FileTime creationTime() {
        return this.creationTime;
    }

    @Override
    public boolean isRegularFile() {
        return this.isRegularFile;
    }

    @Override
    public boolean isDirectory() {
        return this.isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return this.isSymbolicLink;
    }

    @Override
    public boolean isOther() {
        return this.isOther;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public Object fileKey() {
        return this.fileKey;
    }
}
