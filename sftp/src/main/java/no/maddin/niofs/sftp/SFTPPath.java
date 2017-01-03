package no.maddin.niofs.sftp;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

/**
 * A Path implementation for SFTP.
 */
public class SFTPPath implements Path {

    private static final String HOME_PREFIX = "/~/";
    private static final int HOME_PREFIX_LEN = HOME_PREFIX.length();
    private static final String DEFAULT_ROOT_PATH = "";
    private static final String PATH_SEP = "/";
    private final String path;
    private final SFTPHost host;

    SFTPPath(SFTPHost sftpHost, String path) {
        this.host = sftpHost;

        // TODO split the path in ist components
        if (path == null || path.trim().isEmpty()) {
            this.path = DEFAULT_ROOT_PATH;
        } else {
            if (path.startsWith(HOME_PREFIX)) {
                this.path = path.substring(HOME_PREFIX_LEN);
            } else {
                this.path = path;
            }
        }
    }

    @Override
    public FileSystem getFileSystem() {
        return this.host;
    }

    @Override
    public boolean isAbsolute() {
        return path.startsWith(PATH_SEP);
    }

    @Override
    public Path getRoot() {
        if (path.equals(DEFAULT_ROOT_PATH)) {
            return this;
        }
        return new SFTPPath(this.host, DEFAULT_ROOT_PATH);
    }

    @Override
    public Path getFileName() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path getParent() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public int getNameCount() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path getName(int index) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public boolean startsWith(Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public boolean startsWith(String other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public boolean endsWith(String other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path normalize() {
        try {
            URI pathURI = new URI(path);
            return new SFTPPath(this.host, pathURI.normalize().toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(path, e);
        }
    }

    @Override
    public Path resolve(Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path resolve(String other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path resolveSibling(Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path resolveSibling(String other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path relativize(Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public URI toUri() {

        try {
            String userInfo;
            if (host.getUserName() != null) {
                userInfo = host.getUserName() + ':' + host.getPassword();
            } else {
                userInfo = null;
            }
            return new URI("sftp", userInfo, host.getHost(), host.getPort(), this.path, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(path, e);
        }
    }

    @Override
    public Path toAbsolutePath() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Iterator<Path> iterator() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    String getPathString() {
        return this.path;
    }
}