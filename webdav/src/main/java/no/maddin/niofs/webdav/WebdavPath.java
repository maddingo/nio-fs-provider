/*
 Copyright 2012-2023 University of Stavanger, Norway
 
 Portions contributed by Andrew Goh http://github.com/ag88

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

import jakarta.validation.constraints.NotNull;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.*;
import java.util.function.BiPredicate;


/**
 * Denotes a WebDAV Path.
 * 
 * @author Copyright 2012-2023 University of Stavanger, Norway<br>
 * @author Martin Goldhahn, (https://github.com/maddingo)<br>
 * @author Andrew Goh (https://github.com/ag88)<p>
 * 
 * This is an implemtation of java.nio.file.Path for Webdav nio filesystem provider.<p> 
 * 
 * The current implementaion uses the simplified notion that an absolute path is a path that begins with 
 * "/", followed by a set of path elements that define the path. e.g. "/a/b/c" is an absolute path.
 * while "a/b/c" is a relative path.
 * 
 * This implementation attempts to be similar with 'file://' implementation for Unix (e.g. UnixPath).
 * However, as WebDAV is after all different from a normal (unix) filesystem, there are various differences.
 * 
 * <h2>{@link WebdavPath#toAbsolutePath()}</h2>
 * For reading and writing files, an absolute path is required.
 *
 */
@SuppressWarnings("java:S1192")
public class WebdavPath implements Path {

    private final String pathSeparator;
    private final String rootPath;
    private final ArrayList<String> elements;
    private final WebdavFileSystem host;
    private final boolean isAbsolute;

	WebdavPath(WebdavFileSystem webdavHost, String path) {
        this.host = webdavHost;
        this.elements = new ArrayList<>();
		this.pathSeparator = webdavHost.getSeparator();
		if (this.host == null) {
			this.isAbsolute = false;
		} else if (path == null) {
			this.isAbsolute = false;
		} else {
			this.isAbsolute = path.startsWith(pathSeparator);
		}
		this.rootPath = this.isAbsolute ? pathSeparator : "";

		if (path != null) {
			parsePathStr(path);
		}
    }

    WebdavPath(WebdavFileSystem webdavHost, ArrayList<String> elements, boolean absolute) {
        this.host = webdavHost;
        pathSeparator = webdavHost.getSeparator();
        rootPath = absolute ? pathSeparator : "";
        this.elements = elements;
        this.isAbsolute = absolute;
    }
    
    private void parsePathStr(@NotNull String path) {
		if (pathSeparator.equals(path)) {
			return;
		}
		if(this.isAbsolute) {
			path = path.substring(1);
		}
		if(path.endsWith(pathSeparator)) {
			path = path.substring(0, path.length() - 1); //omit trailing slash
		}
		String[] ps = path.split(pathSeparator);
        Collections.addAll(elements, ps);
	}

	@NotNull
    private String getPathString() {
		return rootPath + String.join(pathSeparator, elements);
    }
    
    /**
     * Returns path as string
     *
     * @return string
     */
	@NotNull
    @Override
    public String toString() {    
    	return getPathString();
    }

    /**
     * Returns the file system.
     * Would be an instance of {@link WebdavFileSystem}}
     * 
     * @return filesystem
     */
    @Override
    public FileSystem getFileSystem() {
        return this.host;
    }

    /**
     * Checks if is absolute.
     *
     * @return true, if is absolute
     */
    @Override
    public boolean isAbsolute() {
        return isAbsolute;
    } 

	/**
	 * {@inheritDoc}
	 */
	@NotNull
    @Override
    public Path toAbsolutePath() {
    	if(isAbsolute()) {
			return this;
		} else {
			throw new UnsupportedOperationException("Cannot resolve a relative path to absolute path");
		}
	}

    /**
	 * {@inheritDoc}
     */
    @Override
    public Path getFileName() {
    	if(elements.isEmpty()) {
			return null;
    	} else {
            return new WebdavPath(this.host, elements.get(elements.size()-1));
    	}    		
    }

    /**
	 * {@inheritDoc}
     */
    @Override
    public Path getName(int index) {
		if (!isAbsolute) {
			throw new UnsupportedOperationException("Operation not supported for relative paths");
		}
		return new WebdavPath(host, elements.get(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameCount() {
        return elements.size();
    }

    /**
	 * {@inheritDoc}
     */
    @Override
    public Iterator<Path> iterator() {
    	ArrayList<Path> ret = new ArrayList<>(elements.size());
		for (String element : elements) {
			ret.add(new WebdavPath(host, element));
		}
    	return ret.iterator();
    }

    /**
	 * {@inheritDoc}
     */
    @Override
    public Path getParent() {
        if (elements.isEmpty()) {
			return null;
		}
		return new WebdavPath(host, rootPath + String.join(pathSeparator, elements.subList(0, elements.size()-1)));
    }

    /**
	 * {@inheritDoc}
     */
    @Override
    public Path getRoot() {
    	if (isAbsolute) {
			return new WebdavPath(this.host, rootPath);
		} else {
			return null;
		}
    }

    /**
	 * {@inheritDoc}
     */
    @Override
    public int compareTo(Path other) {
    	if (!(other instanceof WebdavPath)) {
			throw new ClassCastException("Need to be an instance of WebdavPath");
		}
    	
    	WebdavPath ow = (WebdavPath) other;
		int hostCompare = Objects.toString(host, "").compareTo(Objects.toString(ow.host, ""));
    	if (hostCompare != 0) {
			return hostCompare;
		}
		return getPathString().compareTo(ow.getPathString());
    }
    
    /**
	 * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {

    	if(!(other instanceof WebdavPath)) {
			return false;
		}
    	
    	WebdavPath ow = (WebdavPath) other;

		return Objects.equals(host, ow.host)
			&& isAbsolute == ow.isAbsolute
			&& Objects.equals(elements, ow.elements)
			&& Objects.equals(rootPath, ow.rootPath)
			&& Objects.equals(pathSeparator, ow.pathSeparator);
    }

	@Override
	public int hashCode() {
		return Objects.hash(pathSeparator, rootPath, elements, host, isAbsolute);
	}

	/**
	 * {@inheritDoc}
     */
    @Override
    public boolean startsWith(Path other) {
		return startsWithEndsWith(other, String::startsWith);
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public boolean startsWith(String other) {
		return this.getPathString().startsWith(other);
    }

	private boolean startsWithEndsWith(Path other, @NotNull BiPredicate<String, String> predicate) {
		if (!(other instanceof WebdavPath)) {
			return false;
		}

		WebdavPath wp = (WebdavPath) other;
		if (!Objects.equals(host, wp.host)) {
			return false;
		}

		return predicate.test(getPathString(), wp.getPathString());

	}

    /**
	 * {@inheritDoc}
	 */
    @Override
    public boolean endsWith(Path other) {
		return startsWithEndsWith(other, String::endsWith);
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public boolean endsWith(String other) {
    	return endsWith(new WebdavPath(this.host, other));
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Path normalize() {
        try {
            URI normal = new URI(getPathString()).normalize();
            return new WebdavPath(this.host, normal.getPath());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(getPathString(), e);
        }
    }

	/**
	 * {@inheritDoc}
	 * @throws UnsupportedOperationException
	 */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... arg2) throws IOException {
        throw new UnsupportedOperationException();
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Path relativize(@NotNull Path other) {
    	if(!(other instanceof WebdavPath)) {
			throw new IllegalArgumentException("Need to be an instance of WebdavPath");
		}

    	if((isAbsolute() ^ other.isAbsolute())) {
			throw new IllegalArgumentException(
				"Both this path and the other path has to be both absolute or relative");
		}

		WebdavPath wp = (WebdavPath) other;
		if (host != null && wp.host != null && !host.equals(wp.host)) {
			throw new IllegalArgumentException("Both paths must have the same filesystem");
		} else if (host == null && wp.host != null) {
			throw new IllegalArgumentException("If this path doesn't have a filesystem, the other path must not have a filesystem");
		}

    	ListIterator<String> io = wp.elements.listIterator();
    	ListIterator<String> ia = elements.listIterator();
    	while(ia.hasNext()) {
    		String a = ia.next(); 
    		if(io.hasNext()) {
    			String so = io.next();
    			if(!so.equals(a)) {
					throw new IllegalArgumentException("the other path does not start with this path");
				}
    		}
    	}

    	ArrayList<String> re = new ArrayList<>(other.getNameCount() - elements.size());
    	//copy remaining elements
    	while(io.hasNext()) {
    		re.add(io.next());
    	}

    	//returns a relative path
        return new WebdavPath(host, re, false);
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Path resolve(Path other) {    	

		if(!(other instanceof WebdavPath)) {
			throw new IllegalArgumentException("Need to be an instance of WebdavPath");
		}

		if (other.isAbsolute()) {
			return other;
		}

		// empty path and not absolute
		if (other.getNameCount() == 0) {
			return this;
		}

    	WebdavPath wp = (WebdavPath) other;
    	ArrayList<String> ret = new ArrayList<>(elements.size() + wp.elements.size());
    	ret.addAll(elements);
    	ret.addAll(wp.elements);
        
        return new WebdavPath(host, ret, isAbsolute());
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Path resolve(String other) {
        return resolve(new WebdavPath(this.host, other));
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Path resolveSibling(Path other) {
    	if(!(other instanceof WebdavPath)) {
			throw new IllegalArgumentException("Need to be an instance of WebdavPath");
		}

		if(other.isAbsolute()) {
			return other;
		}

		if(other.getNameCount() == 0) {
			return getParent();
		}
		
    	if(getParent()==null) {
    		return new WebdavPath(this.host, new ArrayList<>(), false).resolve(other);
    	} else {
			return getParent().resolve(other);
		}
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Path resolveSibling(String other) {
    	return resolveSibling(new WebdavPath(this.host, other));
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
    	if(elements.isEmpty()) {
			throw new IllegalArgumentException("empty path");
		}
    	if( beginIndex < 0 || endIndex < beginIndex || endIndex > elements.size() ) {
			throw new IllegalArgumentException("index out ot bounds");
		}
    	
    	ArrayList<String> subp = new ArrayList<>(elements.subList(beginIndex, endIndex));
    	
    	return new WebdavPath(this.host, subp, false);
    }

	/**
	 * {@inheritDoc}
	 * @throws UnsupportedOperationException
	 */
    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

	/**
	 * {@inheritDoc}
	 * @throws UnsupportedOperationException
	 */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public URI toUri() {
        String scheme = (host.provider() instanceof WebdavsFileSystemProvider) ? "https" : "http";
        String server = host.getHost();
        int port = host.getPort();

        URI sardineUri;
        try {
            sardineUri = new URI(scheme, null, server, port, getPathString(), null, null);
            return sardineUri;
        } catch(URISyntaxException e) {
            throw new IOError(e);
        }
    }
}
