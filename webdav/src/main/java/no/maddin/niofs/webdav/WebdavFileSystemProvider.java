/*
 Copyright 2012-2013 University of Stavanger, Norway

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package no.maddin.niofs.webdav;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;

/**
 * The WebDAV FileSystemProvider based on Sardine.
 */
public class WebdavFileSystemProvider extends FileSystemProvider {

    private static final int DEFAULT_PORT = 80;
    private final Map<URI, WebdavFileSystem> hosts = new HashMap<>();

    @Override
    public void copy(Path fileFrom, Path fileTo, CopyOption... options) throws IOException {

        if (!(fileFrom instanceof WebdavPath)) {
            throw new IllegalArgumentException(fileFrom.toString());
        }

        if (!(fileTo instanceof WebdavPath)) {
            throw new IllegalArgumentException(fileTo.toString());
        }

        WebdavPath wPathTo = (WebdavPath)fileTo;

        WebdavFileSystem webdavHost = (WebdavFileSystem)fileTo.getFileSystem();

        Sardine webdav = webdavHost.getSardine();

        webdav.put(wPathTo.toUri().toString(), Files.readAllBytes(fileFrom));
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {

        if (!(dir instanceof WebdavPath)) {
            throw new IllegalArgumentException(dir.toString());
        }

        WebdavPath wDir = (WebdavPath)dir;

        WebdavFileSystem webdavHost = (WebdavFileSystem)dir.getFileSystem();

        Sardine webdav = webdavHost.getSardine();

        createDirectoryRecursive(webdav, wDir, attrs);
    }

    private void createDirectoryRecursive(Sardine webdav, WebdavPath wDir, FileAttribute<?>[] attrs) throws IOException {

        if (webdav.exists(wDir.toUri().toString())) {
            return;
        }

        WebdavPath parent = (WebdavPath)wDir.getParent();
        if (parent != null) {
            createDirectoryRecursive(webdav, parent, attrs);
        }
        webdav.createDirectory(wDir.toUri().toString());
    }

    @Override
    public void delete(Path dir) throws IOException {
        if (!(dir instanceof WebdavPath)) {
            throw new IllegalArgumentException(dir.toString());
        }

        WebdavPath wDir = (WebdavPath)dir;
        WebdavFileSystem webdavHost = (WebdavFileSystem)dir.getFileSystem();

        Sardine webdav = webdavHost.getSardine();

        String dirString = "";
        try {
            dirString = wDir.toUri().toString();
            webdav.delete(dirString);
        } catch(SardineException se) {
            if (se.getCause() instanceof IOException) {
                throw (IOException)se.getCause();
            }
            if (Objects.equals(se.getResponsePhrase(), "Not Found")) {
                throw new NoSuchFileException(dirString);
            }
            throw new IOException(se);
        }
    }

    /**
     * The default implementation in FileSystemProvider will simply call
     * delete() in deleteIfExists() and silently ignore any NoSuchFileException.
     * In case of Nexus, trying to delete() will result in 503 (Not allowed)
     * even if the path points to nowhere.
     */
    @Override
    public boolean deleteIfExists(Path path) throws IOException {
        WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
        final String s = path.toUri().toString();
        final boolean exists = webdavFs.getSardine().exists(s);
        return exists;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        try {
            return getWebdavHost(uri, true);
        } catch(URISyntaxException ex) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    @Override
    public Path getPath(URI uri) {
        try {
            WebdavFileSystem host = getWebdavHost(uri, true);
            return new WebdavPath(host, uri.getPath());
        } catch(URISyntaxException e) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    private WebdavFileSystem getWebdavHost(URI uri, boolean create) throws URISyntaxException {
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = DEFAULT_PORT;
        }
        String userInfo = uri.getUserInfo();
        URI serverUri = new URI(getScheme(), userInfo, host, port, null, null, null);

        synchronized (hosts) {
            WebdavFileSystem fs = hosts.get(serverUri);
            if (fs == null && create) {
                fs = new WebdavFileSystem(this, serverUri);
                hosts.put(serverUri, fs);
            }
            return fs;
        }
    }

    @Override
    public String getScheme() {
        return "webdav";
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
        final String s = path.toUri().toString();
        final boolean exists = webdavFs.getSardine().exists(s);
        if (!exists) {
            throw new NoSuchFileException(s);
        }
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException
    {
        return new SardineChannel((WebdavPath)path);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path arg0, Filter<? super Path> arg1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        try {
            return getWebdavHost(uri, true);
        } catch(URISyntaxException e) {
            throw new FileSystemException(e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        WebdavFileSystem wfs = (WebdavFileSystem)path.getFileSystem();
        List<DavResource> resources = wfs.getSardine().getResources(path.toUri().toString());
        if (resources.size() != 1) {
            throw new IllegalArgumentException();
        }
        final DavResource res = resources.get(0);

        if (!type.isAssignableFrom(WebdavFileAttributes.class)) {
            throw new ProviderMismatchException();
        }

        return (A)new WebdavFileAttributes(res);
    }

    @Override
    public Map<String, Object> readAttributes(Path arg0, String arg1, LinkOption... arg2) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path arg0, String arg1, Object arg2, LinkOption... arg3) throws IOException {
        throw new UnsupportedOperationException();
    }
}
