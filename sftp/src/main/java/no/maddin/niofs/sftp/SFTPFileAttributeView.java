package no.maddin.niofs.sftp;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SFTPFileAttributeView implements PosixFileAttributeView {
    private final SFTPPath path;
    private final SFTPFileSystemProvider provider;
    private final LinkOption[] options;

    public SFTPFileAttributeView(SFTPFileSystemProvider sftpFileSystemProvider, SFTPPath path, LinkOption[] options) {
        this.path = path;
        this.provider = sftpFileSystemProvider;
        this.options = options;
    }

    @Override
    public String name() {
        return "posix";
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
        BasicFileAttributes attributes = readAttributes();
        if (!(attributes instanceof PosixFileAttributes)) {
            throw new UnsupportedOperationException("File attributes are not PosixFileAttributes");
        } else {
            return ((PosixFileAttributes)attributes).owner();
        }
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PosixFileAttributes readAttributes() throws IOException {
        return provider.readAttributes((Path)path, PosixFileAttributes.class, options);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        provider.setTimes(path, lastModifiedTime, lastAccessTime, createTime);
    }

    @Override
    public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
        provider.setPermissions(path, perms);
    }

    @Override
    public void setGroup(GroupPrincipal group) throws IOException {
        throw new UnsupportedOperationException();
    }
}
