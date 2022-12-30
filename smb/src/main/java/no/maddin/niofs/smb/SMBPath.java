package no.maddin.niofs.smb;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

/**
 * Denotes a path in an SMB share.
 */
public class SMBPath implements Path {

    private final SMBShare share;
    private final String path;

    SMBPath(SMBShare share, String path) {
        this.share = share;
        this.path = path;
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
        return null;
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
        return 0;
    }

    
    @Override
    public Path getName(int index) {
        return null;
    }

    
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return null;
    }

    @Override
    public boolean startsWith(Path other) {
        return false;
    }

    @Override
    public boolean endsWith(Path other) {
        return false;
    }

    
    @Override
    public Path normalize() {
        return null;
    }

    @Override
    public Path toAbsolutePath() {
        return this;
    }

    
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return null;
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    
    @Override
    public WatchKey register(WatchService watcher,  WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
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
                this.share.toString().compareTo(smbPath.share.toString());
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
        throw new UnsupportedOperationException();
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

