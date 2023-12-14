package no.maddin.niofs.sftp;

import jakarta.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.*;

/**
 * A Path implementation for SFTP.
 */
public class SFTPPath implements Path {

    static final String PATH_SEP = "/";
    private final String path;
    private final SFTPHost host;
    private final java.util.List<String> parts;

    SFTPPath(SFTPHost sftpHost, String path) {
        this.host = sftpHost;
        parts = splitParts(path);
        this.path = "/" + String.join(PATH_SEP, parts);
    }

    private static List<String> splitParts(String path) {
        if (path == null || !path.startsWith(PATH_SEP)) {
            throw new IllegalArgumentException("Path must start with " + PATH_SEP);
        }
        Deque<String> parts = new ArrayDeque<>();
        try {
            for (String p : path.substring(1).split(PATH_SEP, -1)) {
                if (p.isEmpty() || p.equals(".")) {
                    // ignore
                } else if (p.equals("..")) {
                    parts.removeLast();
                } else {
                    parts.add(p);
                }
            }
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException(path, ex);
        }
        return new ArrayList<>(parts);
    }

    private String combineParts(int startIdx, int endIdx) {
        StringBuilder sb = new StringBuilder(PATH_SEP);
        for (String part : parts.subList(startIdx, endIdx)) {
            if (sb.length() > 0) {
                sb.append(PATH_SEP);
            }
            sb.append(part);
        }
        return sb.toString();
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
        if (path ==  null) {
            return this;
        }
        return new SFTPPath(this.host, null);
    }

    @Override
    public Path getFileName() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Path getParent() {

        if (path == null) {
            return null;
        }
        return new SFTPPath(this.host, combineParts(0, getNameCount() - 1));
    }

    @Override
    public int getNameCount() {

        return parts.size();
    }

    @Override
    public @NotNull Path getName(int index) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull Path subpath(int beginIndex, int endIndex) {
        return new SFTPPath(beginIndex == 0 ? host : null, combineParts(0, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        if (other.getFileSystem().equals(this.getFileSystem())) {
            if (other instanceof SFTPPath) {
                SFTPPath otherPath = (SFTPPath) other;
                return Collections.indexOfSubList(this.parts, otherPath.getParts()) == 0;
            }
        }
        return false;
    }

    @Override
    public boolean startsWith(@NotNull String other) {
        Path p = Paths.get(URI.create(other));
        return this.startsWith(p);
    }

    @Override
    public boolean endsWith(@NotNull Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public boolean endsWith(@NotNull String other) {
        return false;
    }

    /**
     * SFTPPAths are normalized at creation time. This just returns itself.
     */
    @Override
    public @NotNull Path normalize() {
        return this;
    }

    @Override
    public @NotNull Path resolve(@NotNull Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull Path resolve(@NotNull String other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull Path resolveSibling(@NotNull Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull Path resolveSibling(@NotNull String other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull Path relativize(@NotNull Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull URI toUri() {

        try {
            String userInfo = null;
            if (host.getUsername() != null) {
                StringBuilder uinfoSb = new StringBuilder();
                uinfoSb.append(host.getUsername());
                if (host.getPassword() != null) {
                    uinfoSb.append(':').append(host.getPassword());
                }
                userInfo = uinfoSb.toString();
            }
            return new URI("sftp", userInfo, host.getHost(), host.getPort(), this.path, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(path, e);
        }
    }

    @Override
    public @NotNull Path toAbsolutePath() {
        return normalize();
    }

    @Override
    public @NotNull Path toRealPath(LinkOption @NotNull ... options) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher, Kind<?> @NotNull [] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher, Kind<?> @NotNull ... events) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull Iterator<Path> iterator() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public int compareTo(@NotNull Path other) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public String getPathString() {
        return this.path;
    }

    public List<String> getParts() {
        return Collections.unmodifiableList(parts);
    }
}