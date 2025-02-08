package no.maddin.niofs.webdav;

import java.nio.file.attribute.*;
import java.time.LocalDate;
import java.util.*;

import com.github.sardine.DavResource;

/**
 * File attributes for WebDAV.
 */
public class WebdavFileAttributes implements PosixFileAttributes {
    private final DavResource res;

    WebdavFileAttributes(DavResource res) {
        this.res = res;
    }

    @Override
    public long size() {
        return res.getContentLength();
    }

    @Override
    public FileTime lastModifiedTime() {
        return Optional.ofNullable(res.getModified()).map(Date::getTime).map(FileTime::fromMillis).orElse(null);
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
        return Optional.ofNullable(res.getCreation()).map(Date::getTime).map(FileTime::fromMillis).orElse(null);
    }

    @Override
    public UserPrincipal owner() {
        return null;
    }

    @Override
    public GroupPrincipal group() {
        return null;
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        if (res.isDirectory()) {
            return EnumSet.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        } else {
            return EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        }
    }
}
