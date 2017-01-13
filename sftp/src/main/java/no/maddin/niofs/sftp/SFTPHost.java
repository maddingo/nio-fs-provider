package no.maddin.niofs.sftp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

/**
 * Represents a host for the SFTP file system provider.
 *
 */
public class SFTPHost extends FileSystem {

    private final FileSystemProvider provider;
    private final int port;
    private final String host;
    private final String password;
    private final String username;

    SFTPHost(FileSystemProvider provider, URI serverUri) {
        this.provider = provider;
        this.host = serverUri.getHost();
        this.port = serverUri.getPort();
        String userInfo = serverUri.getUserInfo();
        String un = null;
        String pw = null;
        if (userInfo != null) {
            String[] ui = userInfo.split(":");
            un = ui[0];
            if (ui.length > 1) {
                pw = ui[1];
            }
        }
        username = un;
        password = pw;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getSeparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path getPath(String first, String... more) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    String getUserName() {
        return this.username;
    }

    String getPassword() {
        return this.password;
    }

    String getHost() {
        return this.host;
    }

    int getPort() {
        return this.port;
    }
}
