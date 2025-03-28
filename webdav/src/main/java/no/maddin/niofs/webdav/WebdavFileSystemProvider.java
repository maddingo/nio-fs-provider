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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.sardine.DavAcl;
import com.github.sardine.DavPrincipal;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * The WebDAV FileSystemProvider based on Sardine.
 */
public class WebdavFileSystemProvider extends FileSystemProvider {

	Logger log = Logger.getLogger(WebdavFileSystemProvider.class.getName());

    private static final int DEFAULT_PORT = 80;
    private final Map<URI, WebdavFileSystem> hosts = new HashMap<>();

    public WebdavFileSystemProvider() {
		super();
	}

	@Override
    public void copy(Path fileFrom, Path fileTo, CopyOption... options) throws IOException {
		log.fine("copy(file from, file to)");

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
    	log.fine("createDirectory");

        if (!(dir instanceof WebdavPath)) {
            throw new IllegalArgumentException(dir.toString());
        }

        WebdavPath wDir = (WebdavPath)dir;

        WebdavFileSystem webdavHost = (WebdavFileSystem)dir.getFileSystem();

        Sardine webdav = webdavHost.getSardine();

        createDirectoryRecursive(webdav, wDir);
    }

    private void createDirectoryRecursive(Sardine webdav, WebdavPath wDir) throws IOException {

    	log.fine("createDirectoryRecursive");
    	
        if (webdav.exists(wDir.toUri().toString())) {
            return;
        }

        WebdavPath parent = (WebdavPath)wDir.getParent();
        if (parent != null) {
            createDirectoryRecursive(webdav, parent);
        }
        webdav.createDirectory(wDir.toUri().toString());
    }

    @Override
    public void delete(Path dir) throws IOException {    	
    	log.fine("delete");

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
    	log.fine("deleteIfExists");
        WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
        final String s = path.toUri().toString();
        if (webdavFs.getSardine().exists(s)) {
            if (!webdavFs.getSardine().list(s).isEmpty()) {
                throw new DirectoryNotEmptyException(s);
            }
            delete(path);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
    	log.fine("getFileSystem");
        try {
            return getWebdavHost(uri, true);
        } catch(URISyntaxException ex) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    @Override
    public Path getPath(URI uri) {
    	log.fine("getPath");
        try {
            WebdavFileSystem host = getWebdavHost(uri, true);
            return new WebdavPath(host, uri.getPath());
        } catch(URISyntaxException e) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    private WebdavFileSystem getWebdavHost(URI uri, boolean create) throws URISyntaxException {

    	log.fine("getWebdavHost");
    	
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = DEFAULT_PORT;
        }
        String userInfo = uri.getUserInfo();
        URI serverUri = new URI(getScheme(), userInfo, host, port, uri.getPath(), null, null);

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
    	log.fine("getScheme");
        return "webdav";
    }

    /**
     * Unsupported
     */
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    	log.fine("getFileAttributeView");
        if (!(path instanceof WebdavPath)) {
            throw new IllegalArgumentException(String.valueOf(path));
        }
        WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
        final String s = path.toUri().toString();
        try {
            Sardine sardine = webdavFs.getSardine();
            DavAcl acl = sardine.getAcl(s);
            List<String> principalCollectionSet = sardine.getPrincipalCollectionSet(s);
            List<DavPrincipal> principals = sardine.getPrincipals(s);

            return (V) new WebdavFileAttributeView((WebdavPath)path, acl, principalCollectionSet, principals);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Unsupported
     */
    @Override
    public FileStore getFileStore(Path path) {
    	log.fine("getFileAttributeView");
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
    	log.fine("checkAccess");
		WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
		final String s = path.toUri().toString();
		final boolean exists = webdavFs.getSardine().exists(s);
		if (!exists) {
			throw new NoSuchFileException(s);
		}
    }

    /**
     * Unsupported
     */
    @Override
    public boolean isHidden(Path path) {
    	log.fine("isHidden");
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public boolean isSameFile(Path path, Path path2) {
    	log.fine("isSameFile");
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public void move(Path source, Path target, CopyOption... options) {
    	log.fine("move");
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException
    {
    	log.fine("newByteChannel");
        return new SardineChannel((WebdavPath)path, options);
    }
    
    private static class WebdavDirStream implements DirectoryStream<Path> {
    	
    	private final ArrayList<Path> paths;
    	
    	private WebdavDirStream(ArrayList<Path> paths) {
    		this.paths = paths;
		}
    	
		@Override
		public void close() {
		}

		@Override
		public Iterator<Path> iterator() {
			return paths.iterator();
		}
    }
    
    @Override
	public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> filter) throws IOException {
		log.fine("newDirectoryStream");

		if (!(path instanceof WebdavPath)) {
			throw new IOException("Need to be an instance of WebdavPath");
		}

		WebdavFileSystem wfs = (WebdavFileSystem) path.getFileSystem();
		Cache<Path, WebdavFileAttributes> cache = wfs.getAttcache();

		List<DavResource> resources = wfs.getSardine().list(path.toUri().toString(), 1, true);

		ArrayList<Path> paths = new ArrayList<>(10);
		Iterator<DavResource> iter = resources.iterator();
		boolean first = true;
		while (iter.hasNext()) {
			DavResource res = iter.next();
			if (first) {
				first = false;
				if (res.isDirectory()) {
					/*
					 * in a canonical directory listing, the parent directory queried isn't included
					 * only its contents this omits that entry so that it 'looks' like a
					 * conventional directory listing
					 */
					WebdavPath dp = new WebdavPath((WebdavFileSystem) path.getFileSystem(), res.getPath());
					if (dp.equals(path))
						continue;
				}
			}
			WebdavPath wpath = new WebdavPath((WebdavFileSystem) path.getFileSystem(), res.getPath());
			if(filter != null && !filter.accept(wpath))
				continue;
			if (cache.getIfPresent(wpath) == null) {
				WebdavFileAttributes attr = new WebdavFileAttributes(res);
				cache.put(wpath, attr);
			}
			paths.add(wpath);
		}

        return new WebdavDirStream(paths);
	}

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    	log.fine("newFileSystem");
        try {
            return getWebdavHost(uri, true);
        } catch(URISyntaxException e) {
            throw new FileSystemException(e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) 
    		throws IOException
	{
        if (type == null || !type.isAssignableFrom(WebdavFileAttributes.class)) {
            throw new UnsupportedOperationException("attribute type " + type + " not supported");
        }

        log.fine("readAttributes(path,type)");
    	if(!(path.getFileSystem() instanceof WebdavFileSystem)) {
    		log.warning("readAttributes(path,type): Invalid filesystem");
    		throw new FileSystemException("Invalid filesystem");
    	}    		
    	
    	Cache<Path, WebdavFileAttributes> cache = ((WebdavFileSystem) path.getFileSystem()).getAttcache();
        WebdavFileAttributes attr = cache.getIfPresent(path);
    	if (attr == null) {

            WebdavFileSystem wfs = (WebdavFileSystem) path.getFileSystem();
            List<DavResource> resources = wfs.getSardine().list(path.toUri().toString(), 0, true);

            //List<DavResource> resources = wfs.getSardine().list(path.toUri().toString());
            if (resources.size() != 1) {
                throw new IllegalArgumentException();
            }
            final DavResource res = resources.get(0);

            attr = new WebdavFileAttributes(res);
            cache.put(path, attr);
        }
		return (A) attr;
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... arg2) throws IOException {
        //throw new UnsupportedOperationException();
        log.fine("readAttributes(path,sattr)");

        if (!(path.getFileSystem() instanceof WebdavFileSystem)) {
            throw new FileSystemException("Invalid filesystem");
        }

        Cache<Path, WebdavFileAttributes> cache = ((WebdavFileSystem) path.getFileSystem()).getAttcache();
        WebdavFileAttributes wattr = cache.get(path, (path1 -> {
            WebdavFileSystem wfs = (WebdavFileSystem) path1.getFileSystem();
            List<DavResource> resources;
            try {
                resources = wfs.getSardine().list(path1.toUri().toString(), 0, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (resources.size() != 1) {
                throw new IllegalArgumentException();
            }
            final DavResource res = resources.get(0);
            return new WebdavFileAttributes(res);
        }));

        if (wattr == null) {
            throw new IOException("Failed to read attributes");
        }
    	TreeMap<String, Object> map = new TreeMap<>();
    	String[] attr = attributes.split(",");
    	for(String a: attr) {
    		switch(a) {
    		case "lastModifiedTime":
    			map.put("lastModifiedTime", wattr.lastModifiedTime());
    			break;
    		case "lastAccessTime":
    			map.put("lastAccessTime", wattr.lastAccessTime());
    			break;
    		case "creationTime":
    			map.put("creationTime", wattr.creationTime());
    			break;
    		case "size":
    			map.put("size", wattr.size());
    			break;
    		case "isRegularFile":
    			map.put("isRegularFile", wattr.isRegularFile());
    			break;
    		case "isDirectory":
    			map.put("isDirectory", wattr.isDirectory());
    			break;
    		case "isSymbolicLink":
    			map.put("isSymbolicLink", wattr.isSymbolicLink());
    			break;
    		case "isOther":
    			map.put("isOther", wattr.isSymbolicLink());
    			break;
    		case "fileKey":
    			map.put("fileKey", wattr.fileKey());
    			break;
    		}    			
    	}
    	
    	return map;
    }

    /**
     * Unsupported
     */
    @Override
    public void setAttribute(Path arg0, String arg1, Object arg2, LinkOption... arg3) {
        log.fine("setAttribute");
    	throw new UnsupportedOperationException();
    }
}
