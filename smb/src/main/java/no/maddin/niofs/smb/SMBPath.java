package no.maddin.niofs.smb;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Denotes a path in an SMB share.
 */
public class SMBPath implements Path {

    private final SMBShare share;
    private final String path;

    private final String[] parts;

    SMBPath(SMBShare share, String path) {
        this.share = share;
        this.path = path;
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        String[] parts = path.split("/");
        List<String> partsList = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.trim().length() > 0) {
                partsList.add(part);
            }
        }
        this.parts = partsList.toArray(new String[0]);
    }

    @Override
    public FileSystem getFileSystem() {
        return share;
    }

    SMBShare getShare() {
        return share;
    }

    String getSmbPath() {
        return path;
    }

    @Override
    public boolean isAbsolute() {
        return share != null;
    }

    @Override
    public Path getRoot() {
        return new SMBPath(share, "/");
    }

    @Override
    public Path getFileName() {
        return null;
    }

    @Override
    public Path getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNameCount() {
        return parts.length;
    }

    @Override
    public Path getName(int index) {
        return new SMBPath(share, '/' + parts[index]);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startsWith(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startsWith(String other) {
        return false;
    }

    @Override
    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean endsWith(String other) {
        return false;
    }


    @Override
    public Path normalize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path toAbsolutePath() {
        throw new UnsupportedOperationException();
    }

    
    @Override
    public Path toRealPath(LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    
    @Override
    public WatchKey register(WatchService watcher,  WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return null;
    }

    @Override
    public Iterator<Path> iterator() {
        return null;
    }

    @Override
    public int compareTo(Path other) {
        if (other instanceof SMBPath) {
            SMBPath smbPath = (SMBPath) other;
            if (Objects.equals(this.share, smbPath.share)) {
                String p0 = this.path == null ? "" : this.path;
                String p1 = smbPath.path == null ? "" : smbPath.path;
                return p0.compareTo(p1);
            } else {
                return this.share.toString().compareTo(smbPath.share.toString());
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SMBPath) {
            SMBPath smbPath = (SMBPath) other;
            return this.compareTo(smbPath) == 0;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int hashCode() {
        return Objects.hash(share, path);
    }

    
    @Override
    public String toString() {
        return Optional.ofNullable(share).flatMap(o -> Optional.of(o.toString())).orElse("") + path;
    }

    @Override
    public URI toUri() {
        try {
            return share.toUri().resolve(URLEncoder.encode(path, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path relativize(Path other) {
        if (other instanceof SMBPath) {
            SMBPath otherPath = (SMBPath) other;
            if (!this.getFileSystem().equals(other.getFileSystem())) {
                throw new IllegalArgumentException("Filesystems are different");
            }
            Path relPath = Paths.get(this.path).relativize(Paths.get(otherPath.path));
            // if the path starts with '/' it is an absolute Path
            return new SMBPath(relPath.isAbsolute() ? share : null, relPath.toString());
        } else {
            throw new IllegalArgumentException("path is not a smb path");
        }
    }

    @Override
    public Path resolve(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolveSibling(Path other) {
        return null;
    }

    @Override
    public Path resolveSibling(String other) {
        return null;
    }

    @Override
    public Path resolve(Path other) {
        throw new UnsupportedOperationException();
    }
}
