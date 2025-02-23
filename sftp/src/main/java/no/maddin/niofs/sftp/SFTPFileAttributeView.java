package no.maddin.niofs.sftp;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.attribute.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SFTPFileAttributeView implements PosixFileAttributeView {
    private final SFTPPath path;
    private final SFTPFileSystemProvider provider;
    private final List<LinkOption> options;

    public SFTPFileAttributeView(SFTPFileSystemProvider sftpFileSystemProvider, SFTPPath path, LinkOption[] options) {
        this.path = path;
        this.provider = sftpFileSystemProvider;
        this.options = options != null ? Arrays.asList(options) : Collections.emptyList();
    }

    @Override
    public String name() {
        return "posix";
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PosixFileAttributes readAttributes() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        throw new UnsupportedOperationException();
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
