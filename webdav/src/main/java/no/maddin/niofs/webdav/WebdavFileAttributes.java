package no.maddin.niofs.webdav;

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
        return res.getContentLength();
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.fromMillis(res.getModified().getTime());
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
