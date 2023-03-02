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
import java.net.ProxySelector;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

/**
 * WebDAV implementation of a FileSystem.
 */
public class WebdavFileSystem extends FileSystem {

    private final FileSystemProvider provider;
    private final int port;
    private final String host;
    private final String password;
    private final String username;
    
    private String rootpath;

    Cache<Path, WebdavFileAttributes> attcache;

    /**
     *
     * @param provider an instance of a WebdavFileSystemProvided. This can be a shared instance.
     * @param serverUri URI for the WEBDAV server, the scheme is ignored.
     */
    public WebdavFileSystem(WebdavFileSystemProvider provider, URI serverUri) {
        this.provider = provider;
        this.host = serverUri.getHost();
        this.port = serverUri.getPort();
        
        if (serverUri.getUserInfo() != null) {
        	String[] ui = serverUri.getUserInfo().split(":");
        	this.username = ui[0];
        	if(ui.length > 1)
        		this.password = ui[1];
        	else 
        		this.password = null;
        } else {
        	this.username = null;
        	this.password = null;
        }
        this.rootpath = getSeparator();
        if( serverUri.getPath() != null && ! serverUri.getPath().equals(""))
        	this.rootpath = serverUri.getPath();
        
		this.attcache = Caffeine.newBuilder()
				   .expireAfterWrite(5, TimeUnit.MINUTES)
				   .maximumSize(1000)
				   .build();

    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    /**
     * Not implemented
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * Not implemented
     * @return null
     */
    @Override
    public Iterable<FileStore> getFileStores() {
        return null;
    }

    @Override
    public Path getPath(String first, String... more) {
        String path;
        if (more.length == 0) {
            path = first;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            for (String segment: more) {
                if (segment.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(getSeparator());
                    }
                    sb.append(segment);
                }
            }
            path = sb.toString();
        }
        return new WebdavPath(this, path);
    }

    /**
     * Not implemented
     * @return null
     */
    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return null;
    }

    /**
     * Not implemented
     * @return null
     */
    @Override
    public Iterable<Path> getRootDirectories() {
    	final Path rootp = new WebdavPath(this, rootpath);
    	
    	Iterable<Path> ret = new Iterable<Path>() {			
			@Override
			public Iterator<Path> iterator() {
				ArrayList<Path> ap = new ArrayList<Path>(1);
				ap.add(rootp);
				return ap.iterator();
			}
		};
    	
        return ret;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    /**
     * Not implemented
     * @return null
     */
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    /**
     * Not implemented
     * @return false
     */
    @Override
    public boolean isOpen() {
        return false;
    }

    /**
     * Not implemented
     * @return false
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Not implemented
     * @return null
     */
    @Override
    public WatchService newWatchService() throws IOException {
        return null;
    }

    /**
     * Not implemented
     * @return null
     */
    @Override
    public Set<String> supportedFileAttributeViews() {
        return null;
    }

    /**
     * Check if one filesystem is equal to another. Checks Host, username and Port.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WebdavFileSystem)) {
            throw new IllegalArgumentException("Argument must be of instance WebdavFileSystem");
        }
        WebdavFileSystem current = (WebdavFileSystem)other;

        return current.host.equals(this.host) && current.username.equals(this.username) && current.port == this.port;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getUserName() {
        return this.username;
    }

    String getHost() {
        return this.host;
    }

    int getPort() {
        return this.port;
    }

    public String getPassword() {
        return this.password;
    }

    Sardine getSardine() throws IOException {
    	if(!(username == null && password == null))
    		return SardineFactory.begin(username, password, ProxySelector.getDefault());
    	else
    		return SardineFactory.begin();     		    		
    }

	public Cache<Path, WebdavFileAttributes> getAttcache() {
		return attcache;
	}

	public void setAttcache(Cache<Path, WebdavFileAttributes> attcache) {
		this.attcache = attcache;
	}

    
    
}
