package no.maddin.niofs.smb;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
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
        parts = Optional.ofNullable(path)
            .map(p -> p.split("/")).stream()
            .flatMap(Arrays::stream)
            .filter(Predicate.not(String::isBlank))
            .toArray(String[]::new);
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
    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException();
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
    public int compareTo(Path other) {
        if (other instanceof SMBPath smbPath) {
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
        if (other instanceof SMBPath smbPath) {
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
        return share.toString() + path;
    }

    @Override
    public URI toUri() {
        return share.toUri().resolve(URLEncoder.encode(path, StandardCharsets.UTF_8));
    }

    @Override
    public Path relativize(Path other) {
        if (other instanceof SMBPath otherPath) {
            if (!this.getFileSystem().equals(other.getFileSystem())) {
                throw new IllegalArgumentException("Filesystems are different");
            }

            URI relativeUri = URI.create(this.path).relativize(URI.create(otherPath.path));
            // if the path starts with '/' it is an absolute Path
            return new SMBPath(relativeUri.getRawPath().startsWith("/") ? share : null, relativeUri.getPath());
        } else {
            throw new IllegalArgumentException("path is not a smb path");
        }
    }

    @Override
    public Path resolve(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolve(Path other) {
        throw new UnsupportedOperationException();
    }
}

