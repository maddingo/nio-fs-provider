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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.model.Allprop;
import com.github.sardine.model.Propfind;

/**
 * The WebDAV FileSystemProvider based on Sardine.
 */
public class WebdavFileSystemProvider extends FileSystemProvider {

	Logger log = LogManager.getLogger(WebdavFileSystemProvider.class);
	
	private static final String NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH = "Need to be an instance of WebdavPath";
    private static final int DEFAULT_PORT = 80;
    private final Map<URI, WebdavFileSystem> hosts = new HashMap<>();       

    public WebdavFileSystemProvider() {
		super();
	}

	@Override
    public void copy(Path fileFrom, Path fileTo, CopyOption... options) throws IOException {
    	Marker marker = MarkerManager.getMarker("copy(file from, file to)");
    	log.debug(marker, "");

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
    	Marker marker = MarkerManager.getMarker("createDIrectory");
    	log.debug(marker, "");

        if (!(dir instanceof WebdavPath)) {
            throw new IllegalArgumentException(dir.toString());
        }

        WebdavPath wDir = (WebdavPath)dir;

        WebdavFileSystem webdavHost = (WebdavFileSystem)dir.getFileSystem();

        Sardine webdav = webdavHost.getSardine();

        createDirectoryRecursive(webdav, wDir, attrs);
    }

    private void createDirectoryRecursive(Sardine webdav, WebdavPath wDir, FileAttribute<?>[] attrs) throws IOException {

    	Marker marker = MarkerManager.getMarker("createDirectoryRecursive");
    	log.debug(marker, "");
    	
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
    	Marker marker = MarkerManager.getMarker("delete");
    	log.debug(marker, "");

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
    	Marker marker = MarkerManager.getMarker("deleteIfExists");
    	log.debug(marker, "");
        WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
        final String s = path.toUri().toString();
        return webdavFs.getSardine().exists(s);
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
    	Marker marker = MarkerManager.getMarker("getFileSystem");
    	log.debug(marker, "");
        try {
            return getWebdavHost(uri, true);
        } catch(URISyntaxException ex) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    @Override
    public Path getPath(URI uri) {
    	Marker marker = MarkerManager.getMarker("getPath");
    	log.debug(marker, "");
        try {
            WebdavFileSystem host = getWebdavHost(uri, true);
            return new WebdavPath(host, uri.getPath());
        } catch(URISyntaxException e) {
            throw new FileSystemNotFoundException(uri.toString());
        }
    }

    private WebdavFileSystem getWebdavHost(URI uri, boolean create) throws URISyntaxException {
    	Marker marker = MarkerManager.getMarker("getWebdavHost");
    	log.debug(marker, "");
    	
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
    	Marker marker = MarkerManager.getMarker("getScheme");
    	log.debug(marker, "");
        return "webdav";
    }

    /**
     * Unsupported
     */
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    	Marker marker = MarkerManager.getMarker("getFileAttributeView");
    	log.debug(marker, "");
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public FileStore getFileStore(Path path) throws IOException {
    	Marker marker = MarkerManager.getMarker("getFileAttributeView");
    	log.debug(marker, "");
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
    	Marker marker = MarkerManager.getMarker("checkAccess");
    	log.debug(marker, "");
        try {
			WebdavFileSystem webdavFs = (WebdavFileSystem)path.getFileSystem();
			final String s = path.toUri().toString();
			final boolean exists = webdavFs.getSardine().exists(s);
			if (!exists) {
			    throw new NoSuchFileException(s);
			}
		} catch (NoSuchFileException e) {
			log.error(marker, "path: {}", path.toString());
			log.error(e);
			throw e;
		} catch (IOException e) {
			log.error(marker, "path: {}", path.toString());
			log.error(e);
			throw e;
		}
    }

    /**
     * Unsupported
     */
    @Override
    public boolean isHidden(Path path) throws IOException {
    	Marker marker = MarkerManager.getMarker("isHidden");
    	log.debug(marker, "");
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
    	Marker marker = MarkerManager.getMarker("isSameFile");
    	log.debug(marker, "");
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
    	Marker marker = MarkerManager.getMarker("move");
    	log.debug(marker, "");
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException
    {
    	Marker marker = MarkerManager.getMarker("newByteChannel");
    	log.debug(marker, "");
        return new SardineChannel((WebdavPath)path);
    }

    
    class DirStream implements DirectoryStream<Path> {
    	
    	ArrayList<Path> paths;
    	
    	public DirStream(ArrayList<Path> paths) {
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
    
    @Override
	public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> filter) throws IOException {
		// throw new UnsupportedOperationException();
		Marker marker = MarkerManager.getMarker("newDirectoryStream");
		log.debug(marker, "");

		try {
			if (!(path instanceof WebdavPath)) {
				IOException e = new IOException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);
				log.error(marker, "path {}", path.toString());
				log.error(e);
				throw e;
			}

			WebdavFileSystem wfs = (WebdavFileSystem) path.getFileSystem();
			Cache<Path, WebdavFileAttributes> cache = wfs.getAttcache();

			List<DavResource> resources = wfs.getSardine().list(path.toUri().toString(), 1, true);

			ArrayList<Path> paths = new ArrayList<Path>(10);
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

			DirStream dirstream = new DirStream(paths);

			return dirstream;
		} catch (IOException e) {
			log.error(marker, "path {}", path.toString());
			log.error(e);
			throw e;
		}
	}

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    	Marker marker = MarkerManager.getMarker("newFileSystem");
    	log.debug(marker, "");
        try {
            return getWebdavHost(uri, true);
        } catch(URISyntaxException e) {
            throw new FileSystemException(e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) 
    		throws IOException {
    	Marker marker = MarkerManager.getMarker("readAttributes(path,type)");
    	log.debug(marker, "");
    	if(!(path.getFileSystem() instanceof WebdavFileSystem)) {
    		log.error(marker, "Invalid filesystem");
    		throw new FileSystemException("Invalid filesystem");
    	}    		
    	
    	Cache<Path, WebdavFileAttributes> cache = ((WebdavFileSystem) path.getFileSystem()).getAttcache();
    	if (cache.getIfPresent(path) != null) {
    		return (A) cache.getIfPresent(path);
    	}
    	
        List<DavResource> resources;
		try {
			WebdavFileSystem wfs = (WebdavFileSystem)path.getFileSystem();        
			resources = wfs.getSardine().list(path.toUri().toString(),0,true);
			
	        //List<DavResource> resources = wfs.getSardine().list(path.toUri().toString());
	        if (resources.size() != 1) {
	            throw new IllegalArgumentException();
	        }
	        final DavResource res = resources.get(0);

	        if (!type.isAssignableFrom(WebdavFileAttributes.class)) {
	            throw new ProviderMismatchException();
	        }
	        
	        WebdavFileAttributes attr = new WebdavFileAttributes(res); 
	        cache.put(path, attr);
	        
	        return (A) attr;

		} catch (IOException e) {
			log.warn(marker, "error connecting: {}", path.toUri().toString());
			log.error(e);
			throw e;
		}
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... arg2) throws IOException {
        //throw new UnsupportedOperationException();
    	Marker marker = MarkerManager.getMarker("readAttributes(path,sattr)");    	
    	log.debug(marker, "");
    	
    	WebdavFileAttributes wattr;
    	if(!(path.getFileSystem() instanceof WebdavFileSystem)) {
    		log.error(marker, "Invalid filesystem");
    		throw new FileSystemException("Invalid filesystem");
    	}    		
    	
    	Cache<Path, WebdavFileAttributes> cache = ((WebdavFileSystem) path.getFileSystem()).getAttcache();
    	if (cache.getIfPresent(path) != null) 
    		wattr = cache.getIfPresent(path);
    	else {
            WebdavFileSystem wfs = (WebdavFileSystem)path.getFileSystem();        
            List<DavResource> resources = wfs.getSardine().list(path.toUri().toString(),0,true);
            //List<DavResource> resources = wfs.getSardine().list(path.toUri().toString());
            if (resources.size() != 1) {
                throw new IllegalArgumentException();
            }
            final DavResource res = resources.get(0);

            wattr = new WebdavFileAttributes(res);
            cache.put(path, wattr);
    	}    	
    	    	
    	TreeMap<String, Object> map = new TreeMap<String, Object>();
    	String attr[] = attributes.split(",");
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
    public void setAttribute(Path arg0, String arg1, Object arg2, LinkOption... arg3) throws IOException {
    	Marker marker = MarkerManager.getMarker("setAttribute");    	
    	log.debug(marker, "");
        throw new UnsupportedOperationException();
    }
}
