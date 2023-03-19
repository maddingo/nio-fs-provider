package no.maddin.niofs.smb;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider based on smbj.
 */
public class SMBFileSystemProvider extends FileSystemProvider {

    private static final Logger log = Logger.getLogger(SMBFileSystemProvider.class.getName());
    private final SMBClient client;

    private final Map<URI, SMBShare> fileSystems = new WeakHashMap<>();

    public SMBFileSystemProvider() {

        SmbConfig config = SmbConfig.builder()
            .withTimeout(120, TimeUnit.SECONDS) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
            .withSoTimeout(180, TimeUnit.SECONDS) // Socket Timeout (default is 0 seconds, blocks forever)
            .build();

        client = new SMBClient(config);
    }

    public SMBClient getClient() {
        return client;
    }

    @Override
    public String getScheme() {
        return "smb";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        SMBParts parts = splitURI(uri);
        if (!parts.path.isEmpty()) {
            throw new IllegalArgumentException("The uri should be smb://" + parts.host + parts.share + " optional user info");
        }
        URI shareUri = shareUri(parts);
        SMBShare smbShare = new SMBShare(this, parts.host, parts.share, principal(parts.userinfo, env));
        fileSystems.put(shareUri, smbShare);
        return smbShare;
    }

    private SMBShare.UsernamePassword principal(String userInfo, Map<String, ?> env) {

        return Optional.ofNullable(userInfo)
            .flatMap(upwd -> {
                Pattern userinfoPattern = Pattern.compile("((?<domain>\\w)\\)?(?<user>\\w)\\:(?<pwd>)");
                Matcher matcher = userinfoPattern.matcher(upwd);
                if (matcher.matches()) {
                    String domain = matcher.group("domain");
                    String user = matcher.group("user");
                    String password = matcher.group("pwd");
                    return Optional.of(new SMBShare.UsernamePassword(user, password, domain));
                } else {
                    return Optional.empty();
                }
            })
            .or(() ->
                Optional.ofNullable(env)
                .map(m -> {
                    String username = (String)m.get("USERNAME");
                    String password = Optional.ofNullable((String) m.get("PASSWORD")).orElse(null);
                    String domain = (String) m.get("DOMAIN");
                    return new SMBShare.UsernamePassword(username, password, domain);
                })
            )
            .orElse(null);
    }

    SMBParts splitURI(URI uri) {
        String path = uri.getPath();
        if (path == null || path.length() < 2 || path.indexOf('/') != 0) {
            throw new IllegalArgumentException("incorrect share in path " + path);
        }
        int shareEndPos = path.indexOf('/', 1);
        if  (shareEndPos == -1) {
            shareEndPos = path.length();
        }
        String share = URLDecoder.decode(path.substring(1, shareEndPos), StandardCharsets.UTF_8);
        String file = URLDecoder.decode(path.substring(shareEndPos), StandardCharsets.UTF_8);

        return new SMBParts(uri.getHost(), uri.getPort(), uri.getUserInfo(), share, file);
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        SMBParts parts = splitURI(uri);

        SMBShare share;
        try {
            share = fileSystems.get(shareUri(parts));
            if (share == null) {
                throw new FileSystemNotFoundException();
            }
            return share;
        } catch (IOException e) {
            throw (FileSystemNotFoundException)new FileSystemNotFoundException().initCause(e);
        }
    }

    private URI shareUri(SMBParts parts) throws IOException {
        try {
            return new URI(this.getScheme(), parts.userinfo, parts.host, parts.port, '/' + parts.share, null, null);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Path getPath(URI uri) {
        SMBShare share = (SMBShare) getFileSystem(uri);

        SMBParts parts = splitURI(uri);
        return new SMBPath(share, parts.path);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException
    {
        return new SMBByteChannel(smbPath(path));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        SMBPath smbFile = smbPath(dir);
        if (!smbFile.isAbsolute()) {
            throw new IllegalArgumentException(dir.toString());
        }

        return new SMBDirectoryStream(this, (SMBPath)smbFile, filter);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) {
        ((SMBShare)smbPath(dir).getFileSystem()).getDiskShare().mkdir(dir.toString());
    }

    @Override
    public void delete(Path path) {
        FileAllInformation fileInformation = ((SMBShare) smbPath(path).getFileSystem()).getDiskShare().getFileInformation(path.toString());
        if (fileInformation.getStandardInformation().isDirectory()) {
            ((SMBShare) smbPath(path).getFileSystem()).getDiskShare().rmdir(path.toString(), true);
        } else {
            ((SMBShare) smbPath(path).getFileSystem()).getDiskShare().rm(path.toString());
        }
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) {
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

    @SuppressWarnings("unchecked")
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

    public static SMBPath  smbPath(Path path) {
        if (path instanceof SMBPath sp) {
            return sp;
        }
        throw new ProviderMismatchException();
    }

    public record SMBParts(String host, int port, String userinfo, String share, String path) {
    }
}
