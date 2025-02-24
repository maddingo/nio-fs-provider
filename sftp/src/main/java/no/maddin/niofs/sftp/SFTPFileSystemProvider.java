package no.maddin.niofs.sftp;

import com.jcraft.jsch.*;
import jakarta.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * FileSystemProvider for Secure FTP.
 */
public class SFTPFileSystemProvider extends FileSystemProvider {
    static final String SFTP = "sftp";
    private final Map<URI, SFTPHost> hosts = Collections.synchronizedMap(new HashMap<>());

    private final JSch jsch = new JSch();

    private static final Logger log = Logger.getLogger(SFTPFileSystemProvider.class.getName());

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
    public @NotNull Path getPath(@NotNull URI uri) {
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
        if (!(path instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(path));
        }
        return new JSchByteChannel(jsch, (SFTPPath)path, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        if (!(dir instanceof SFTPPath)) {
            throw new IllegalArgumentException(dir.toString());
        }

        SFTPHost sftpHost = (SFTPHost) dir.getFileSystem();

        try (SFTPSession sftpSession = new SFTPSession(sftpHost, jsch)) {
            List<ChannelSftp.LsEntry> ls = sftpSession.sftp.ls(((SFTPPath)dir).getPathString());

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
            for (String subPath : parts) {
                mkdir(subPath, sftpSession);
                cd(subPath, sftpSession);
            }
        } catch (JSchException e) {
            throw new IOException(e);
        }
    }

    private static void cd(String subPath, SFTPSession sftpSession) throws IOException {
        try {
            sftpSession.sftp.cd(subPath);
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    private static void mkdir(String subPath, SFTPSession sftpSession) throws IOException {
        try {
            sftpSession.sftp.mkdir(subPath);
        } catch(SftpException e) {
            if (!isFileExistsException(e)) {
                throw new IOException(subPath, e);
            }
        }
    }

    private static boolean isFileExistsException(@NotNull SftpException e) {
        return e.id == ChannelSftp.SSH_FX_FAILURE && e.getMessage().contains("file exists");
    }

    @Override
    public void delete(Path path) throws IOException {
        if (!(path instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(path));
        }

        SFTPHost sftpHost = (SFTPHost)path.getFileSystem();
        boolean isDir = false;
        try (SFTPSession sftpSession = new SFTPSession(sftpHost, jsch)) {
            SftpATTRS stat = sftpSession.sftp.stat(((SFTPPath) path).getPathString());
            isDir = stat.isDir();
            sftpSession.sftp.rm(((SFTPPath) path).getPathString());
        } catch(JSchException e) {
            throw new IOException(e);
        } catch (SftpException e) {
            switch (e.id) {
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    throw new NoSuchFileException(path.toString());
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    throw new AccessDeniedException(path.toString());
                case ChannelSftp.SSH_FX_FAILURE:
                    if (isDir) {
                        throw new DirectoryNotEmptyException(path.toString());
                    } else {
                        throw new IOException(e);
                    }
                default:
                    throw new IOException(e);
            }
        }
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        if (!(source instanceof SFTPPath && target instanceof SFTPPath)) {
            throw new UnsupportedOperationException("Both source and target must be associated with the same provider");
        }
      if (source.getFileSystem().equals(target.getFileSystem())) {
            String sourcePath = ((SFTPPath)source).getPathString();
            String targetPath = ((SFTPPath)target).getPathString();
            SFTPHost host = (SFTPHost) source.getFileSystem();
            copySameProvider(host, sourcePath, targetPath, options);
        } else {
            throw new UnsupportedOperationException("Copy between different filesystems not supported");
        }
    }

    private void copySameProvider(SFTPHost host, String source, String target, CopyOption[] options) throws IOException {
        try (SFTPSession sftpSession = new SFTPSession(host, jsch)) {
            if (options != null && options.length > 0) {
                log.info("Copy option is ignored");
            }
            File tmpFile = createTempFile();
            try (
                OutputStream tmpOut = Files.newOutputStream(tmpFile.toPath())
            ) {
                sftpSession.sftp.get(source, tmpOut);
            }
            sftpSession.sftp.put(tmpFile.getAbsolutePath(), target);
        } catch (JSchException | SftpException e) {
            throw new IOException(e);
        }

    }

    private static File createTempFile() throws IOException {
        boolean isPosixFS = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
        File tempFile = isPosixFS ? createPosixTempfile() : creeateNonPosixTempfile();
        tempFile.deleteOnExit();
        return tempFile;
    }

    private static File createPosixTempfile() throws IOException {
        File tempFile;
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
        tempFile = Files.createTempFile("prefix", "suffix", attr).toFile();
        return tempFile;
    }

    @SuppressWarnings({"java:S5443", "java:S899"}) // use of createTempFile is ok here
    private static File creeateNonPosixTempfile() throws IOException {
        File tempFile;
        tempFile = Files.createTempFile("prefix", "suffix").toFile();
        tempFile.setReadable(true, true);
        tempFile.setWritable(true, true);
        tempFile.setExecutable(true, true);
        return tempFile;
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        if (!(path instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(path));
        }
        SFTPPath sftpPath = (SFTPPath) path;
        SFTPHost host = (SFTPHost) sftpPath.getFileSystem();
        try (SFTPSession sftpSession = new SFTPSession(host, jsch)) {
            String pathString = sftpPath.getPathString();
            SftpATTRS stat = sftpSession.sftp.stat(pathString);
            return stat.getPermissions() == 0 || fileName(pathString).startsWith(".");
        } catch (JSchException | SftpException e) {
            throw new FileSystemException(path.toString(), null, e.getMessage());
        }
    }

    private String fileName(String fullPath) {
        int idx = fullPath.lastIndexOf('/');
        return idx == -1 ? fullPath : fullPath.substring(idx + 1);
    }

    @Override
    public FileStore getFileStore(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        if (!(path instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(path));
        }
        SFTPPath sftpPath = (SFTPPath) path;
        SFTPHost host = (SFTPHost) sftpPath.getFileSystem();
        try (SFTPSession sftpSession = new SFTPSession(host, jsch)) {
            SftpATTRS stat = sftpSession.sftp.stat(sftpPath.getPathString());
            if (modes.length == 0) {
                return;
            }
            int permissions = stat.getPermissions();
            for (AccessMode m : modes) {
                switch (m) {
                    case READ:
                        if ((permissions & 0_444) == 0) {
                            throw new FileSystemException(path.toString(), null, "cannot read file");
                        }
                        break;
                    case WRITE:
                        if ((permissions & 0_222) == 0) {
                            throw new FileSystemException(path.toString(), null, "cannot write");
                        }
                        break;
                    case EXECUTE:
                        if ((permissions & 0_111) == 0) {
                            throw new FileSystemException(path.toString(), null, "cannot execute");
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("AccessMode " + m + " not supported");
                }
            }
        } catch (JSchException | SftpException e) {
            throw new FileSystemException(path.toString(), null, e.getMessage());
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        if (!(path instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(path));
        }
        if (type == null || !type.isAssignableFrom(SFTPFileAttributeView.class)) {
            throw new UnsupportedOperationException("Attribute view of type " + type + " not supported");
        }
        return (V)new SFTPFileAttributeView(this, (SFTPPath) path, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if (!(path instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(path));
        }
        if (type == null || !type.isAssignableFrom(SFTPFileAttributes.class)) {
            throw new UnsupportedOperationException("File attribute type " + type + " not supported");
        }
        SFTPPath sftpPath = (SFTPPath) path;
        SFTPHost host = sftpPath.getHost();
        try (SFTPSession sftpSession = new SFTPSession(host, jsch)) {
            SftpATTRS stat = sftpSession.sftp.lstat(sftpPath.getPathString());
            return (A)new SFTPFileAttributes(stat);
        } catch (JSchException | SftpException e) {
            throw new FileSystemException(path.toString(), null, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        createLink(ChannelSftp::symlink, link, target, attrs);
    }

    private void createLink(LinkFunction linkFunction, Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        if (!(link instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(link));
        }
        if (!Objects.equals(link.getFileSystem(), target.getFileSystem())) {
            throw new UnsupportedOperationException("Symbolic links between different filesystems not supported");
        }
        if (!link.isAbsolute()) {
            throw new IllegalArgumentException("link must be absolute");
        }

        SFTPPath sftpLink = (SFTPPath) link;
        SFTPPath sftpTarget = (SFTPPath) target;
        if (sftpLink.getHost() != sftpTarget.getHost()) {
            throw new UnsupportedOperationException("Symbolic links between different hosts not supported");
        }
        SFTPHost host = sftpLink.getHost();
        try (SFTPSession sftpSession = new SFTPSession(host, jsch)) {
            linkFunction.createLink(sftpSession.sftp, sftpLink.getPathString(), sftpTarget.getPathString());
        } catch (JSchException | SftpException e) {
            throw (FileSystemException)new FileSystemException(link.toString(), target.toString(), e.getMessage()).initCause(e);
        }
    }

    @Override
    public void createLink(Path link, Path target) throws IOException {
        createLink(ChannelSftp::hardlink, link, target);
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        if (!(link instanceof SFTPPath)) {
            throw new IllegalArgumentException(String.valueOf(link));
        }
        SFTPPath sftpLink = (SFTPPath) link;
        SFTPHost host = sftpLink.getHost();
        try (SFTPSession sftpSession = new SFTPSession(host, jsch)) {
            return new SFTPPath(host, sftpSession.sftp.readlink(sftpLink.getPathString()));
        } catch (JSchException | SftpException e) {
            throw new FileSystemException(link.toString(), null, e.getMessage());
        }
    }

    void removeCacheEntry(URI serverUri) {
        hosts.remove(serverUri);
    }

    void setPermissions(SFTPPath path, Set<PosixFilePermission> permissions) throws IOException {
        try (SFTPSession sftpSession = new SFTPSession(path.getHost(), jsch)) {
            int fileMask = permissionsToMask(permissions);
            sftpSession.sftp.chmod(fileMask, path.getPathString());
        } catch (JSchException | SftpException e) {
            throw new FileSystemException(path.toString(), null, e.getMessage());
        }
    }

    static int permissionsToMask(Set<PosixFilePermission> permissions) {
        return permissions.stream()
                .mapToInt(p -> {
                    switch (p) {
                        case OWNER_READ:
                            return 0_400;
                        case OWNER_WRITE:
                            return 0_200;
                        case OWNER_EXECUTE:
                            return 0_100;
                        case GROUP_READ:
                            return 0_40;
                        case GROUP_WRITE:
                            return 0_20;
                        case GROUP_EXECUTE:
                            return 0_10;
                        case OTHERS_READ:
                            return 0_4;
                        case OTHERS_WRITE:
                            return 0_2;
                        case OTHERS_EXECUTE:
                            return 0_1;
                        default:
                            return 0;
                    }
                })
                .reduce((res, elem) -> res | elem)
            .orElse(0);
    }

    static class SFTPSession implements AutoCloseable {
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
        public void close() {
            sftp.quit();
            session.disconnect();
        }
    }

    static class SftpDirStream implements DirectoryStream<Path> {

        @NotNull
        private final List<Path> paths;

        public SftpDirStream(List<Path> paths) {
            this.paths = paths == null ? Collections.emptyList() : paths;
        }

        @Override
        public void close() {
            // nothing to do
        }

        @Override
        public @NotNull Iterator<Path> iterator() {
            return paths.iterator();
        }

    }

    @FunctionalInterface
    private interface LinkFunction {
        void createLink(ChannelSftp sftp, String source, String target) throws IOException, SftpException, JSchException;
    }
}
