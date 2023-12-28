package no.maddin.niofs.sftp;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FileSystemProvider for Secure FTP.
 */
public class SFTPFileSystemProvider extends FileSystemProvider {
    static final String SFTP = "sftp";
    private final Map<URI, SFTPHost> hosts = Collections.synchronizedMap(new HashMap<>());

    private final JSch jsch = new JSch();

    public SFTPFileSystemProvider() {
        JSch.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    public String getScheme() {
        return SFTP;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        try {
            return getSFTPHost(uri, true, true);
        } catch (URISyntaxException e) {
            throw new FileSystemException(e.toString());
        }
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        try {
            return getSFTPHost(uri, true, false);
        } catch (URISyntaxException ex) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    @Override
    public Path getPath(URI uri) {
        try {
            SFTPHost host = getSFTPHost(uri, false, true);
            return new SFTPPath(host, uri.getPath());
        } catch (URISyntaxException e) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    /**
     * Get a SFTP Host with the given host, user, password and port.
     *
     * @param uri    valid URI
     * @param create if {@code true} a new SFTPHost is created if none is registered.
     */
    private SFTPHost getSFTPHost(URI uri, boolean requireEmptyPath, boolean create) throws URISyntaxException {
        URI serverUri = SFTPHost.getServerUri(uri, requireEmptyPath);

        SFTPHost fs = hosts.computeIfAbsent(serverUri, u -> {
            if (create) {
                return new SFTPHost(this, u);
            } else {
                return null;
            }
        });
        if (fs == null) {
            throw new FileSystemNotFoundException(uri.toString());
        }
        return fs;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        if (!(dir instanceof SFTPPath)) {
            throw new IllegalArgumentException(dir.toString());
        }

        SFTPHost sftpHost = (SFTPHost) dir.getFileSystem();

        try (SFTPSession sftpSession = new SFTPSession(sftpHost, jsch)) {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> ls = sftpSession.sftp.ls(((SFTPPath)dir).getPathString());

            List<Path> list = ls.stream()
                .map(ChannelSftp.LsEntry::getFilename)
                .filter(fn -> !fn.equals(".") && !fn.equals("..")) // TODO relative filenames not supported
                .map(fn -> "/" + fn) // TODO relative filenames not supported
                .map(fn -> new SFTPPath(sftpHost,fn))
                .filter(p -> {
                    try {
                        if (filter != null) {
                            return filter.accept(p);
                        } else {
                            return true;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

            return new SftpDirStream(list);

        } catch (JSchException | SftpException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        if (!(dir instanceof SFTPPath)) {
            throw new IllegalArgumentException(dir.toString());
        }

        SFTPHost sftpHost = (SFTPHost)dir.getFileSystem();

        try (SFTPSession sftpSession = new SFTPSession(sftpHost, jsch)) {
            List<String> parts = ((SFTPPath) dir).getParts();
            // remove the first part if it is the root directory (empty string)
            if (!parts.isEmpty() && "".equals(parts.get(0))) {
                parts = parts.subList(1, parts.size()-1);
            }
            // Implementation might not support recursive creation of directories
            for (String subPath : parts) {
                try {
                    sftpSession.sftp.mkdir(subPath);
                } catch(SftpException e) {
                    throw new IOException(subPath, e);
                }

            }

        } catch (JSchException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    void removeCacheEntry(URI serverUri) {
        hosts.remove(serverUri);
    }

    private static class SFTPSession implements AutoCloseable {
        private final Session session;
        final ChannelSftp sftp;

        public SFTPSession(SFTPHost host, JSch jsch) throws JSchException {
            this.session = jsch.getSession(host.getUsername(), host.getHost(), host.getPort());
            UserInfo userinfo = new SFTPUserInfo(host.getPassword());
            session.setUserInfo(userinfo);
            session.connect();

            this.sftp = (ChannelSftp)session.openChannel(SFTP);

            sftp.connect();
        }

        @Override
        public void close() throws IOException {
            sftp.quit();
            session.disconnect();
        }
    }

    static class SftpDirStream implements DirectoryStream<Path> {

        List<Path> paths;

        public SftpDirStream(List<Path> paths) {
            this.paths = paths;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public Iterator<Path> iterator() {
            return paths.iterator();
        }

    }


}
