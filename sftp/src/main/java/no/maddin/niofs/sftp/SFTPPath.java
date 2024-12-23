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
    private final List<String> parts;

    SFTPPath(@NotNull SFTPHost sftpHost, String path) {
        this.host = sftpHost;
        if (path == null) {
            this.path = "";
            this.parts = Collections.emptyList();
        } else {
            this.path = path;
            this.parts = splitParts(path);
        }
    }

    private static List<String> splitParts(@NotNull String path) {
        Deque<String> parts = new ArrayDeque<>();
        try {
            for (String p : path.split(PATH_SEP, -1)) {
                if (p.equals("..")) {
                    parts.removeLast();
                } else if (!p.isEmpty() && !p.equals(".")) {
                    parts.add(p);
                }
            }
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException(path, ex);
        }
        return new ArrayList<>(parts);
    }

    private String combineParts(int startIdx, int endIdx) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (path.startsWith(PATH_SEP) && startIdx == 0) {
            sb.append(PATH_SEP);
        }
        for (String part : parts.subList(startIdx, endIdx)) {
            if (!first) {
                sb.append(PATH_SEP);
            }
            sb.append(part);
            first = false;
        }
        return sb.toString();
    }

    @Override
    public FileSystem getFileSystem() {
        return this.host;
    }

    @Override
    public boolean isAbsolute() {
        return path.startsWith(PATH_SEP) && host != null;
    }

    @Override
    public Path getRoot() {
        if (host ==  null) {
            return null;
        }
        return new SFTPPath(this.host, PATH_SEP);
    }

    @Override
    public Path getFileName() {
        if (path.isEmpty()) {
            return null;
        }
        return new SFTPPath(this.host, parts.get(parts.size() - 1));
    }

    @Override
    public Path getParent() {

        if (!path.startsWith(PATH_SEP)) {
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
        if (index < 0 || index >= parts.size()) {
            throw new IllegalArgumentException("index");
        }
        return new SFTPPath(this.host, parts.get(index - 1));
    }

    @Override
    public @NotNull Path subpath(int beginIndex, int endIndex) {
        return new SFTPPath(host, combineParts(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        if (other instanceof SFTPPath && other.getFileSystem().equals(this.getFileSystem())) {
                SFTPPath otherPath = (SFTPPath) other;
                return Collections.indexOfSubList(this.parts, otherPath.getParts()) == 0;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean endsWith(@NotNull String other) {
        if (this.parts.isEmpty()) {
            return false;
        }
        // Remove trailing Separator
        String cleanPath = other.endsWith("/") ? other.substring(0, other.length() - 1) : other;
        return this.parts.get(this.parts.size() - 1).equals(cleanPath);
    }

    @Override
    public @NotNull Path normalize() {
        return new SFTPPath(this.host, combineParts(0, getNameCount()));
    }

    @Override
    public @NotNull Path resolve(@NotNull Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Path resolve(@NotNull String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Path resolveSibling(@NotNull Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Path resolveSibling(@NotNull String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Path relativize(@NotNull Path other) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher, Kind<?> @NotNull [] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher, Kind<?> @NotNull ... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterator<Path> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(@NotNull Path other) {
        throw new UnsupportedOperationException();
    }

    public String getPathString() {
        return this.path;
    }

    public List<String> getParts() {
        return Collections.unmodifiableList(parts);
    }

    @Override
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (host != null && path.startsWith(PATH_SEP)) {
            sb.append(host);
        }
        sb.append(path);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SFTPPath) {
            SFTPPath other = (SFTPPath) obj;
            return Objects.equals(this.host, other.host) && Objects.equals(this.path, other.path);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.host, this.path);
    }
}