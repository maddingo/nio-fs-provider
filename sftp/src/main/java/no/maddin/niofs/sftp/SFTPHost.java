package no.maddin.niofs.sftp;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a host for the SFTP file system provider.
 *
 */
public class SFTPHost extends FileSystem {

    private static final int DEFAULT_PORT = 22;
    private final SFTPFileSystemProvider provider;
    private final int port;
    private final String host;
    private final UserInfo userInfo;

    private final URI serverUri;

    private final AtomicBoolean open = new AtomicBoolean();

    SFTPHost(SFTPFileSystemProvider provider, URI serverUri) {
        this.serverUri = serverUri;
        this.provider = provider;
        this.host = serverUri.getHost();
        this.port = serverUri.getPort();
        this.userInfo = userInfo(serverUri.getUserInfo());
        open.set(true);
    }

    private static UserInfo userInfo(String userInfo) {
        String un = null;
        String pw = null;
        if (userInfo != null) {
            String[] ui = userInfo.split(":");
            un = ui[0];
            if (ui.length > 1) {
                pw = ui[1];
            }
        }
        return new UserInfo(un, pw);
    }

    @NotNull
    static URI getServerUri(URI uri, boolean requireEmptyPath) throws URISyntaxException {
        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException(uri.toString());
        }
        if (requireEmptyPath) {
            if (uri.getRawPath() != null && !uri.getRawPath().isEmpty()) {
                throw new IllegalArgumentException("Path should be empty");
            }
            if (uri.getRawQuery() != null) {
                throw new IllegalArgumentException("Query should be empty");
            }
            if (uri.getRawFragment() != null) {
                throw new IllegalArgumentException("Fragment should be empty");
            }
        }
        int port = uri.getPort();
        if (port == -1) {
            port = DEFAULT_PORT;
        }
        String userInfo = uri.getUserInfo();
        return new URI(SFTPFileSystemProvider.SFTP, userInfo, host, port, null, null, null);
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        open.set(false);
        provider.removeCacheEntry(serverUri);
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public boolean isReadOnly() {
        checkOpen();
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        checkOpen();
        throw new UnsupportedOperationException();
    }

    private void checkOpen() {
        if (!isOpen()) {
            throw new ClosedFileSystemException();
        }
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        checkOpen();
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        checkOpen();
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(String first, String... more) {
        checkOpen();
        checkSameFileSystem(first);
        StringBuilder newPath = new StringBuilder();
        if (first != null && !first.isEmpty()) {
            newPath.append(first);
        }
        for (String m : more) {
            if (m != null && !m.isEmpty()) {
                if (newPath.length() > 0) {
                    newPath.append(SFTPPath.PATH_SEP);
                }
                newPath.append(m);
            }
        }
        URI newUri = URI.create(newPath.toString());
        return new SFTPPath(this, newUri.getPath());
    }

    private void checkSameFileSystem(String uriString) {
        try {
            URI uri = getServerUri(URI.create(uriString), false);
            if (!this.serverUri.equals(uri)) {
                throw new IllegalArgumentException("Filesystems do not match");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        checkOpen();
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        checkOpen();
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        checkOpen();
        throw new UnsupportedOperationException();
    }

    public String getUsername() {
        return userInfo.username;
    }

    public String getPassword() {
        return userInfo.password;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public URI getServerUri() {
        return serverUri;
    }

    public static class UserInfo{
        private final String username;
        private final String password;

        public UserInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
