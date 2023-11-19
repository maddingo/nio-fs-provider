package no.maddin.niofs.webdav;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

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
    	Date d = res.getModified();
    	if (d == null)
    		return null;
    	else 
    		return FileTime.fromMillis(d.getTime());
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
}
