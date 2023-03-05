/*
 Copyright 2012-2013 University of Stavanger, Norway
 
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

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Denotes a WebDAV Path.  
 */
public class WebdavPath implements Path {

    private static final String NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH = "Need to be an instance of WebdavPath";
    private static final String PARENT_PATH = "..";
    //private static final String PATH_SEP = "/";
    final String PATH_SEP;
    //private static final String DEFAULT_ROOT_PATH = PATH_SEP;
    private final String DEFAULT_ROOT_PATH;
    private ArrayList<String> elements;
    private final WebdavFileSystem host;
    
    private boolean isabsolute = false;

    WebdavPath(WebdavFileSystem webdavHost, String path) {
        this.host = webdavHost;
        this.elements = new ArrayList<String>();
        PATH_SEP = webdavHost.getSeparator();
        DEFAULT_ROOT_PATH = PATH_SEP;
        parsePathStr(path);
    }

    WebdavPath(WebdavFileSystem webdavHost, ArrayList<String> elements, boolean absolute) {
        this.host = webdavHost;
        PATH_SEP = webdavHost.getSeparator();
        DEFAULT_ROOT_PATH = PATH_SEP;
        this.elements = elements;
        this.isabsolute = absolute;
    }
    
    private void parsePathStr(String path) {
        assert(path != null);
        
        if (path.equals(DEFAULT_ROOT_PATH)) {
        	//this.elements.add(DEFAULT_ROOT_PATH);
        	this.isabsolute = true;
        	//empty path elements        	
        } else {
            String p = path.trim();
            if(p.startsWith(PATH_SEP)) {
            	this.isabsolute = true;
            	p = p.substring(1);
            }
            if(p.endsWith(PATH_SEP)) 
            	p = p.substring(0,p.length()-1); //omit trailing slash
            String ps[] = p.split(PATH_SEP);
            for (String s : ps)
            	elements.add(s);
        }    	
    }
    
    public String getPathString() {
    	StringBuilder sb = new StringBuilder(100);
		if (isabsolute)
			sb.append(PATH_SEP);
    	    	
    	boolean first = true;
    	for(String f : elements) {
    		if(first) {    			
    			first = false;
    		} else
    			sb.append(PATH_SEP);
    		sb.append(f);    		
    	}
        return sb.toString();
    }
    
    @Override
    public String toString() {    
    	return getPathString();
    }

    
    @Override
    public FileSystem getFileSystem() {
        return this.host;
    }

    @Override
    public boolean isAbsolute() {
        return isabsolute;
    } 

    
    @Override
    public Path toAbsolutePath() {
    	if(isAbsolute())
    		return this;
    	
    	return new WebdavPath(this.host, DEFAULT_ROOT_PATH).resolve(this);
    }


    /**
     * Gets the file name as a Path
     *
     * @return file name as a Path 
     */
    @Override
    public Path getFileName() {
    	if(elements.size()==0) {
    		if (isabsolute)
    			return null;
    		else 
    			return this;
    	} else
    		return new WebdavPath(this.host, elements.get(elements.size()-1));
    }

    @Override
    public Path getName(int index) {     		
    	if(elements.size() == 0 && index==0) {
       		if (isabsolute)
       				throw new IllegalArgumentException();
        		else 
        			return this; 
    	} else if ( index < elements.size() && index >= 0 ) {
    		return new WebdavPath(host, elements.get(index));
    	} else //(index >= elements.size() || index < 0)
    		throw new IllegalArgumentException();
    }

    @Override
    public int getNameCount() {
        return elements.size();
    }

    @Override
    public Iterator<Path> iterator() {
    	ArrayList<Path> ret = new ArrayList<>(elements.size());
    	for(int i=0; i< elements.size(); i++) {
    		Path p = getName(i);
    		ret.add(p);
    	}
    	return ret.iterator();
    }

    
    /**
     * Gets the parent
     * 
     * Returns the parent path.
     * Returns null if this path does not have a parent.
     *
     * @return parent path
     */
    @Override
    public Path getParent() {
        if (elements.size()==0)
        	return null;

        ArrayList<String> elms = new ArrayList<>(elements.size()-1);
        elms.addAll(elements.subList(0, elements.size()-1));
        return new WebdavPath(host, elms, true );
    }

    
    /**
     * Gets the root.
     *
     * Returns the root path, note that this simply returns "/" 
     * it returns null for relative paths
     * 
     * @return rootpath
     */
    @Override
    public Path getRoot() {
    	if (isabsolute)
    		return new WebdavPath(this.host, DEFAULT_ROOT_PATH);
    	else
    		return null;
    }
    

    @Override
    public int compareTo(Path other) {
        //throw new UnsupportedOperationException();
    	
    	if(!(other instanceof WebdavPath))     		
    		throw new ClassCastException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);
    	
    	WebdavPath ow = (WebdavPath) other;
    	
    	if(elements.size() > ow.getElements().size())
    		return 1;
    	else if(elements.size() < ow.getElements().size())
    		return -1;
    	
    	ArrayList<String> oe = ow.getElements();
    	for(int i=0; i<elements.size(); i++) {
    		if(elements.get(i).equals(oe.get(i)))
    			continue;
    		return elements.get(i).compareTo(oe.get(i));
    	}
    	
    	return 0;
    	    	
    }
    
    @Override
    public boolean equals(Object other) {
    	
    	if(!(other instanceof WebdavPath))     		
    		return false;
    	
    	WebdavPath ow = (WebdavPath) other;

    	if(ow.getElements().size() > elements.size() ||
    		ow.getElements().size() < elements.size())
    		return false;
    	
    	ArrayList<String> oe = ow.getElements();
    	for(int i=0; i<elements.size(); i++) {
    		if(elements.get(i).equals(oe.get(i)))
    			continue;
    		return false;
    	}
    	
    	return true;
    }

    @Override
    public boolean startsWith(Path other) {
        //throw new UnsupportedOperationException();
    	if(!(other instanceof WebdavPath))
    		return false;
    	
    	if(other.getNameCount() > getNameCount())
    		return false;
    	    	
    	WebdavPath wp = (WebdavPath) other;
    	if (!((WebdavFileSystem) wp.getFileSystem()).equals(host))
    		return false;
    	
    	ListIterator<String> io = wp.getElements().listIterator();    	
    	while(io.hasNext()) {
    		int i = io.nextIndex();
    		if(io.next().equals(elements.get(i)))
    			continue;    		
    		return false;
    	}
    	
    	return true;
    }

    @Override
    public boolean startsWith(String other) {
        //throw new UnsupportedOperationException();
    	return startsWith(new WebdavPath(this.host, other));
    }
    
    @Override
    public boolean endsWith(Path other) {
        //throw new UnsupportedOperationException();
    	if(!(other instanceof WebdavPath))
    		return false;
    	
    	if(other.getNameCount() > getNameCount())
    		return false;
    	    	
    	WebdavPath wp = (WebdavPath) other;
    	if (!((WebdavFileSystem) wp.getFileSystem()).equals(host))
    		return false;
    	
    	int si = getNameCount() - other.getNameCount();
    	ListIterator<String> io = wp.getElements().listIterator();
    	for(int i=si; i<getNameCount(); i++) {
    		if(io.hasNext())
    			if(elements.get(i).equals(io.next()))
    				continue;
    		return false;
    	}

    	return true;
    }

    @Override
    public boolean endsWith(String other) {
        //throw new UnsupportedOperationException();
    	return endsWith(new WebdavPath(this.host, other));
    }

    @Override
    public Path normalize() {
        try {
            URI normal = new URI(getPathString()).normalize();
            return new WebdavPath(this.host, normal.getPath());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(getPathString(), e);
        }
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... arg2) throws IOException {
        throw new UnsupportedOperationException();
    }

    /*
     * Constructs a relative path between this path and a given path.
     * 
     * relativize() is the inverse of resolve().
     * This method attempts to construct a relative path that is less this path as preceeding path
     * e.g. this "a/b" , other "a/b/c/d" - relativize() = "c/d"
     * 
     * this and other path needs to be both relative
     * or both absolute
     * 
     */
    @Override
    public Path relativize(Path other) {
    	if (other == null)
    		throw new NullPointerException();
    	
    	if(!(other instanceof WebdavPath))
    		throw new IllegalArgumentException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);
    		
    	if( (isAbsolute() && !other.isAbsolute()) ||
    		(!isAbsolute() && other.isAbsolute()))
    		throw new IllegalArgumentException(
    			"Both this path and the other path has to be both absolute or relative");
    	
    	if (other.getNameCount() < this.getNameCount())
    		throw new IllegalArgumentException(
        			"the other path is short than this path");
    	
    	WebdavPath wp = (WebdavPath) other;
    	ListIterator<String> io = wp.getElements().listIterator(); 
    	ListIterator<String> ia = elements.listIterator();
    	while(ia.hasNext()) {
    		String a = ia.next(); 
    		if(io.hasNext()) {
    			String so = io.next();
    			if(!so.equals(a))
   		    		throw new IllegalArgumentException(
    		        	"the other path is short than this path");
    		}
    	}

    	ArrayList<String> re = new ArrayList<String>(other.getNameCount() - elements.size());
    	//copy remaining elements
    	while(io.hasNext()) {
    		re.add(io.next());
    	}
    	//returns a relative path
        return new WebdavPath(host, re, false);        
    }

    /*
     * Resolve the given path against this path.
	 * 
	 * In the simplest case, this method joins the given path to this path 
	 * and returns a resulting path that ends with the given path.
	 * 
	 * @param  other The other path to resolved against this
	 * 
	 * @return If the other parameter is an absolute path, returns other.
	 *         If other is an empty path, returns this path.
	 *         Otherwise this method considers this path to be a directory and
	 *         resolves the given path against this path.           
     */
    @Override
    public Path resolve(Path other) {    	
        if (other == null) 
        	throw new NullPointerException();
        
        if (other.isAbsolute()) 
            return other;        

        // empty path and not absolute
        if (other.getNameCount() == 0)
        	return this;            

    	if(!(other instanceof WebdavPath))
    		throw new IllegalArgumentException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);

    	WebdavPath wp = (WebdavPath) other;
    	ArrayList<String> ret = new ArrayList<String>(elements.size() + wp.getElements().size());
    	ret.addAll(elements);
    	ret.addAll(wp.getElements());
        
        return new WebdavPath(host, ret, isAbsolute());
    }

    @Override
    public Path resolve(String other) {
        return resolve(new WebdavPath(this.host, other));
    }

    /*
     * Resolves the given path against this path's parent path. 
     */
    @Override
    public Path resolveSibling(Path other) {
        //throw new UnsupportedOperationException();
    	if(!(other instanceof WebdavPath))
    		throw new IllegalArgumentException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);

		if(other.isAbsolute())
			return other;

    	if(getParent()==null) {
    		return other;    			
    	} else
    		return getParent().resolve(other);
    	    	
    }

    @Override
    public Path resolveSibling(String other) {
        //throw new UnsupportedOperationException();
    	return resolveSibling(new WebdavPath(this.host, other));
    }


    /*
     * Returns a relative Path that is a subsequence of the name elements of this path.
     * 
     * @param beginIndex begin index
     * @param endIndex end index 
     * @return a relative path with elements from elements[beingIndex] to elements[endIndex-1]
     *         elements[endIndex-1] is included.
     */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        //throw new UnsupportedOperationException();
    	if(elements.size() == 0)
    		throw new IllegalArgumentException();
    	if( beginIndex < 0 || endIndex <= beginIndex || endIndex > elements.size() )
    		throw new IllegalArgumentException();

    	
    	ArrayList<String> subp = new ArrayList<String>(elements.subList(beginIndex, endIndex));
    	
    	return new WebdavPath(this.host, 
    		subp, false);
    	
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

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

	public ArrayList<String> getElements() {
		return elements;
	}

	public void setElements(ArrayList<String> elements) {
		this.elements = elements;
	}	    
    
}
