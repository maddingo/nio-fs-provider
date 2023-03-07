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
 * 
 * @author Copyright 2012-2023 University of Stavanger, Norway<br>
 * @author Martin Goldhahn, (https://github.com/maddingo)<br>
 * @author Andrew Goh (https://github.com/ag88)<p>
 * 
 * This is an implemtation of java.nio.file.Path for Webdav nio filesystem provider.<p> 
 * 
 * The current implementaion uses the simplified notion that an absolute path is a path that begins with 
 * "/", followed by a set of path elements that define the path. e.g. "/a/b/c" is an absolute path.
 * while "a/b/c" is a relative path. The paths needs to be normalized, paths with "." and ".." in the 
 * elements are not (yet) supported.<p>  
 * 
 * This implementation attempts to be similar with 'file://' implementation for Unix (e.g. UnixPath).
 * However, as WebDAV is after all different from a normal (unix) filesystem, there are various differences.
 * 
 * <h2>{@link WebdavPath#toAbsolutePath()}</h2>
 * 
 * For {@link WebdavPath#toAbsolutePath()}, for paths that are absolute paths, it simply return itself.<p>
 * 
 * However, for relative paths, the paths are altered. In 'file://' (unix) implementation,
 * Path.toAbsolutePath() apparently resolves relative path against the current working directory.<p>
 * 
 * However, with web and WebDAV url it is difficult to define 'current working directory'.
 * To make this implementation more usable, {@link WebdavPath}{@link #toAbsolutePath()} use a
 * 'current working path' instance variable to resolve relative paths into absolute paths.
 * If 'current working path' is not found (null), it is resolved against the default root path "/".<p> 
 * 
 * The prefix absolute path are preserved when relative paths are created with several methods.
 * <ul> 
 * <li> WebdavPath.relativize(Path path) </li>
 * <li> WebdavPath.getFileName() </li>
 * <li> WebdavPath.getName(int index) </li>
 * </ul>
 * <p>
 * To get predictable behavior e.g. that it would be resolved against "current work path"
 * it is necessary that the <b><em>relative path</em></b> is derived by calling the above methods against
 * base objects that are absolute paths. e.g.<p>
 * '/a/b'.relativize('/a/b/c') = 'c', '/a/b' preserved as "current work path", 
 * 'c'.toAbsolutePath() returns '/a/b/c' <br>
 * '/a/b/c'.getFileName() = 'c', '/a/b' preserved as "current work path",
 * 'c'.toAbsolutePath() returns '/a/b/c' <br>
 * '/a/b/c'.getName(1) = 'b', '/a' preserved as "current work path", 
 * 'b'.toAbsolutePath() returns '/a/b'<p>
 *  
 * However, it is not saved in the cases where relative paths are used as base path<p>
 * 'a/b'.relativize('a/b/c') = 'c', current work path is not saved as 'a/b' is a relative path<p>
 * hence, 'c'.toAbsolutePath() returns '/c'<p>
 * 
 * the current work path can be retrieved and set using the methods<br>
 * <ul> 
 * <li> WebdavPath.getCurrentWorkPath() </li>
 * <li> WebdavPath.setCurrentWorkPath(WebdavPath path) </li>
 * </ul><p>
 * 
 * However, the currentworkpath needs to be set before calling toAbsolutePath()
 * and the currentworkpath must not be null to have it resolved that way deterministically
 * 
 * <h2>Normalized paths only</h2>
 * 
 * Current implementation of {@link WebdavPath} requires that paths are normalized.
 * i.e. that they do not contain ".." or "." that needs to be resolved.<br>
 * and they contain distinct and unique path elements e.g. "/a/b/c".
 * If they do contain ".." or ".", these are treated as literals
 *
 * 
 * @see WebdavPath#toAbsolutePath()
 * @see WebdavPath#relativize(Path path)  
 * @see WebdavPath#getFileName()
 * @see WebdavPath#getName(int index)
 * @see WebdavPath#getCurrentWorkPath()
 * @see WebdavPath#setCurrentWorkPath(WebdavPath path)
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
    private WebdavPath currentWorkPath = null; 
    
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
    
    private String getPathString() {
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
    
    /**
     * Returns path as string
     *
     * @return string
     */
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
        return isabsolute;
    } 

    
	/**
	 * To absolute path.
	 * 
	 * If this path is an absolute path, this method simply returns this.<p>
	 * 
	 * If this path is a relative path, this method attempts to return this path
	 * resolved against the "current work path".<p>
	 * 
	 * If however, if "current work path" is not found it resolves it against the
	 * default root path "/".<p>
	 * 
	 * The "current work path" is formed and preserved when the following methods
	 * are called:
	 *
	 * <ul>
	 * <li>WebdavPath.relativize(Path path)</li>
	 * <li>WebdavPath.getFileName()</li>
	 * <li>WebdavPath.getName(int index)</li>
	 * </ul><p>
	 * 
	 * However, with web and WebDAV url it is difficult to define 'current working
	 * directory'. To make this implementation more usable,
	 * {@link WebdavPath}{@link #toAbsolutePath()} use a 'current working path'
	 * instance variable to resolve relative paths into absolute paths. If 'current
	 * working path' is not found (null), it is resolved against the default
	 * root path "/".
	 * <p>
	 * 
	 * To get predictable behavior e.g. that it would be resolved against "current
	 * work path" it is necessary that the relative path is derived by calling the
	 * above methods against base objects that are absolute paths. e.g.
	 * <p>
	 * '/a/b'.relativize('/a/b/c') = 'c', '/a/b' preserved as "current work path",
	 * 'c'.toAbsolutePath() returns '/a/b/c' <br>
	 * '/a/b/c'.getFileName() = 'c', '/a/b' preserved as "current work path",
	 * 'c'.toAbsolutePath() returns '/a/b/c' <br>
	 * '/a/b/c'.getName(1) = 'b', '/a' preserved as "current work path",
	 * 'b'.toAbsolutePath() returns '/a/b'
	 * <p>
	 * 
	 * However, it is not saved in the cases where relative paths are used as the base<p>
	 * 
	 * 'a/b'.relativize('a/b/c') = 'c', current work path is not saved as 'a/b/c' is
	 * a relative path<p>
	 * 
	 * hence, 'c'.toAbsolutePath() returns '/c'<p>
	 * 
	 * The current work path can be retrieved and set using the methods<br>
	 * <ul>
	 * <li>WebdavPath.getCurrentWorkPath()</li>
	 * <li>WebdavPath.setCurrentWorkPath(WebdavPath currentworkpath)</li>
	 * </ul>
	 * <p>
	 * 
	 * However, the currentworkpath needs to be set before calling toAbsolutePath()
	 * and the currentworkpath must not be null to have it resolved that way
	 * deterministically
	 * 
	 * @see WebdavPath#relativize(Path path)
	 * @see WebdavPath#getFileName()
	 * @see WebdavPath#getName(int index)
	 * @see WebdavPath#getCurrentWorkPath()
	 * @see WebdavPath#setCurrentWorkPath(WebdavPath path)
	 */
    @Override
    public Path toAbsolutePath() {
    	if(isAbsolute())
    		return this;
    	
    	if(currentWorkPath != null)     		
    		return currentWorkPath.resolve(this);
    	else
    		return new WebdavPath(this.host, DEFAULT_ROOT_PATH).resolve(this);
    }


    /**
     * Gets the file name of the leaf in the path<p>
     *
     * If this path is the empty root path "/" it returns null.<br>
     * If this path is ann empty relative path it returns an empty string ""<br>
     * Returns the filename as a path in normal cases<p>
     *  
     * If this path is an absolute path, this method preserves the 
     * parent in "current work path" in the returned filename path.
     * 
     * @return file name as a Path<br>
     * null for root path<br>
     * blank for empty relative path
     */
    @Override
    public Path getFileName() {
    	if(elements.size()==0) {
    		if (isabsolute)
    			return null;
    		else 
    			return this;
    	} else {
    		WebdavPath wp = new WebdavPath(this.host, elements.get(elements.size()-1));
    		if (isabsolute)
    			wp.setCurrentWorkPath((WebdavPath) getParent());    		
    		return wp; 
    	}    		
    }

    /**
     * Returns the name at index in the path.
     *
     * If this path is an absolute path, this method preserves the 
     * parent in "current work path" in the returned filename path.
     *
     * @param index the index
     * @return name at index
     * @throws IllegalArgumentException if this is an empty root path and index = 0
     * @throws IllegalArgumentException if index is out of bounds
     */
    @Override
    public Path getName(int index) {     		
    	if(elements.size() == 0 && index==0) {
       		if (isabsolute)
       				throw new IllegalArgumentException("empty root path");
        		else 
        			return this; 
    	} else if ( index < elements.size() && index >= 0 ) {
    		WebdavPath wp = new WebdavPath(host, elements.get(index));
    		if(isabsolute) {
    			wp.setCurrentWorkPath((WebdavPath) subpath(0, index));
    		}
    			
    		return wp;
    	} else //(index >= elements.size() || index < 0)
    		throw new IllegalArgumentException("index out of bounds");
    }

    /**
     * Gets the name count.
     *
     * @return the name count
     */
    @Override
    public int getNameCount() {
        return elements.size();
    }

    /**
     * return the name paths in an iterator<p>
     * 
     * from the root to leaf (left to right)
     *
     * @return iterator of name paths
     */
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
     * Gets the parent.
     * 
     * Returns the parent path.<p>
     * Returns null if this path does not have a parent.<p>
     * 
     * note that parent of "/" is null.
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
     * Gets the root path.
     *
     * Returns the root path, note that this simply returns "/".<p> 
     * It returns null for relative paths
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
    

    /**
     * Compare to the other path.
     *
     * Paths with number of elements less than this is deem less and vice versa.<p>
     * Otherwise it compares the elements each from root to leaf as a string<p>
     * The difference from String.compareTo() is returned for the different member
     * 
     * @param other the other path
     * @return int = 0 if the other is equal<br>
     *             &lt; 0 if other is less than this<br>
     *             &gt; 0 if other is greater than this<br>
     */
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
    
    /**
     * Equals this<p>
     *
     * This and other both must be relative or absolute to be equal.<br>
     * Paths with number of elements different from this is deemed not equal.<br>
     * Otherwise it compares the elements each from root to leaf as a string.<br>
     * Returns true only if every element match 

     * @param other the other path
     * @return true, if successful
     */
    @Override
    public boolean equals(Object other) {
    	
    	if(!(other instanceof WebdavPath))     		
    		return false;
    	
    	WebdavPath ow = (WebdavPath) other;

    	if(this.isabsolute != ow.isAbsolute())
    		return false;
    	
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

    /**
     * Starts with.
     *
     * compare elements from root to leaf, returns true if they match
     * 
     * both this and other needs to be the same host
     * both this and other needs to be relative or absolute
     * 
     * @param other the other needs to be an instance of {@link WebdavPath}
     * @return true, if successful
     */
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
    	
    	if((this.isabsolute && !other.isAbsolute()) ||
    	   (!this.isabsolute && other.isAbsolute()))
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

    /**
     * Starts with.
     *
     * converts string other to WebdavPath and call startsWith(Path other)
     *
     * @param other the other
     * @return true, if successful
     */
    @Override
    public boolean startsWith(String other) {
        //throw new UnsupportedOperationException();
    	return startsWith(new WebdavPath(this.host, other));
    }
    
    /**
     * Ends with.
     * 
     * compare elements alighed to leaf, returns true if they match
     * refer to {@link Path#endsWith(Path)}
     * 
     * both this and other needs to be the same host
     * other needs to be relative path 
     * 
     * @param other the other
     * @return true, if successful
     */
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
    	
    	if(!isabsolute && other.isAbsolute())
    		return false;
    	
    	if (other.isAbsolute())
    		return equals(other);
    	
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

    /**
     * Ends with.
     * 
     * converts string other to WebdavPath and call endssWith(Path other)
     *
     * @param other the other
     * @return true, if successful
     */
    @Override
    public boolean endsWith(String other) {
        //throw new UnsupportedOperationException();
    	return endsWith(new WebdavPath(this.host, other));
    }

    
    /**
     * Normalize.
     * @see Path#normalize()
     *
     * @return the path
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
     * Register
     *
     * not supported
     * 
     * @param watcher the watcher
     * @param events the events
     * @return the watch key
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws UnsupportedOperationException not supported.
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Register
     *
     * not supported
     * 
     * @param watcher the watcher
     * @param events the events
     * @param arg2 the arg 2
     * @return the watch key
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws UnsupportedOperationException not supported
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... arg2) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Constructs a relative path between this path and a given path.<p>
     * 
     * relativize() is the inverse of resolve().<p>
     * This method attempts to construct a relative path that is less this path as preceeding path<p>
     * e.g. this "/a/b" , other "/a/b/c/d" - '/a/b'.relativize('/a/b/c/d') returns "c/d"<p>
     * 
     * This and other path needs to be both relative or both absolute.<p>
     * 
     * If this path is an absolute path, the returned relative path would contain a "current work path",
     * which is the leading common part of the absolute path dropped to construct the relative path
     * returned. Normally, that "current work path" is this path.<p>
     * 
     * The current implementation requires that both paths are normalized.<br>
     * i.e. that they do not contain ".." or "." that needs to be resolved.<br>
     * and they contain distinct and unique path elements e.g. "/a/b/c".<br>
     * If they do contain ".." or ".", these are treated as literals 
     *
     * @param other the other path
     * @return path relativized path
     * @throws IllegalArgumentException if the other path is not a WebdavPath
     * @throws IllegalArgumentException if one path is absolute while the other is not
     * @throws IllegalArgumentException if the other path is shorter than this path
     * @throws IllegalArgumentException if the other path does not start with this path
     * @see WebdavPath#toAbsolutePath()
     * @see WebdavPath#resolve(Path)
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
        			"the other path is shorter than this path");
    	
    	WebdavPath wp = (WebdavPath) other;
    	ListIterator<String> io = wp.getElements().listIterator(); 
    	ListIterator<String> ia = elements.listIterator();
    	ArrayList<String> workpath = new ArrayList<>(elements.size()/2);
    	while(ia.hasNext()) {
    		String a = ia.next(); 
    		if(io.hasNext()) {
    			String so = io.next();
    			if(so.equals(a))
    				workpath.add(so); //preserve the workpath while doing
    			else
   		    		throw new IllegalArgumentException(
    		        	"the other path does not start with this path");
    		}
    	}

    	ArrayList<String> re = new ArrayList<String>(other.getNameCount() - elements.size());
    	//copy remaining elements
    	while(io.hasNext()) {
    		re.add(io.next());
    	}
    	
    	
    	//returns a relative path
    	WebdavPath relpath = new WebdavPath(host, re, false);
    	//save the workpath
    	if (isabsolute) {    		
    		relpath.setCurrentWorkPath(new WebdavPath(this.host, workpath, true));
    	}    		    	
        return relpath;         
    }

    /**
     * Resolve the given path against this path.
	 * 
	 * In the simplest case, this method joins the given path to this path 
	 * and returns a resulting path that ends with the given path.<br>
	 * Normally, the other path needs to be a relative path<p>
	 * 
	 * e.g. '/a/b'.resolve('c/d') = '/a/b/c/d'<p>
	 * 
	 * If the other path is an absolute path, returns other.<br>
	 * If other is an empty path, returns this path.<p>
	 * 
	 * Otherwise this method considers this path to be a directory and
	 * resolves the given path against this path.
	 * 
	 * @param  other The other path to resolved against this
	 * 
	 * @return If the other parameter is an absolute path, returns other.<br>
	 *         If other is an empty path, returns this path.<br>
	 *         Otherwise this method considers this path to be a directory and
	 *         resolves the given path against this path.
	 *         
	 * @see WebdavPath#relativize(Path)
	 * @see WebdavPath#toAbsolutePath()
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

    /**
     * Resolve the given path (string) against this path.
     *
     * converts to WebdavPath and calls resolve(other)
     * 
     * @param other the other
     * @return the path
     */
    @Override
    public Path resolve(String other) {
        return resolve(new WebdavPath(this.host, other));
    }

    /**
     * Resolve sibling.
     * 
     * In normal case, resolves/returns the given path against this path's parent path.<p>
     *  
     * If the other path is absolute, returns the other path.<br>
     * If there is no parent, returns the other path<br>
     * If the other is an empty (relative) path, returns this path<br>
     * If this path do not have a parent, returns an empty path ""
     * 
     * @param other the other path
     * @return path resolved path
     */
    @Override
    public Path resolveSibling(Path other) {
        //throw new UnsupportedOperationException();
    	if(!(other instanceof WebdavPath))
    		throw new IllegalArgumentException(NEED_TO_BE_AN_INSTANCE_OF_WEBDAV_PATH);

		if(other.isAbsolute())
			return other;

		if(other.getNameCount() ==0)
			return getParent();
		
    	if(getParent()==null) {
    		return new WebdavPath(this.host, new ArrayList<String>(), false).resolve(other);    			
    	} else
    		return getParent().resolve(other);
    	    	
    }

    /**
     * Resolve sibling.
     * 
     * Converts to WebdavPath and calls resolveSibling(other)
     *
     * @param other the other
     * @return the path
     */
    @Override
    public Path resolveSibling(String other) {
        //throw new UnsupportedOperationException();
    	return resolveSibling(new WebdavPath(this.host, other));
    }


    /**
     * Returns a relative Path that is a subsequence of the name elements of this path.
     * 
     * @param beginIndex begin index
     * @param endIndex end index 
     * @return a relative path with elements from elements[beingIndex] to elements[endIndex-1]
     *         elements[endIndex-1] is included.
     * @throws IllegalArgumentException if this path is empty.<br>
     *         IllegalArgumentException if index is out of bounds
     */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        //throw new UnsupportedOperationException();
    	if(elements.size() == 0)
    		throw new IllegalArgumentException("empty path");
    	if( beginIndex < 0 || endIndex < beginIndex || endIndex > elements.size() )
    		throw new IllegalArgumentException("index out ot bounds");
    	
    	ArrayList<String> subp = new ArrayList<String>(elements.subList(beginIndex, endIndex));
    	
    	return new WebdavPath(this.host, subp, false);
    	
    }

    /**
     * To file
     * unsupported 
     * 
     * @return the file
     * @throws UnsupportedOperationException not supported
     * 
     */
    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    /**
     * To real path
     * unsupported
     * 
     * @param options the options
     * @return the path
     * @throws UnsupportedOperationException not supported
     */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    
    /**
     * To uri.
     * 
     * returns URI represented by this path
     *
     * @return uri URI represented by this path
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

	/**
	 * Gets the internal elements of this path<p>
	 * 
	 * Specific to WebdavPath, not in {@link Path} api<p>
	 * not recommended to use this, use those documented in {@link Path} api instead 
	 *
	 * @return the elements
	 */
	public ArrayList<String> getElements() {
		return elements;
	}

	/**
	 * Sets the internal elements of this path<p>
	 * 
	 * Specific to WebdavPath, not in {@link Path} api<p>
	 * not recommended to use this, use those documented in {@link Path} api instead  
	 *
	 * @param elements the new elements
	 */
	public void setElements(ArrayList<String> elements) {
		this.elements = elements;
	}

	
	/**
	 * Gets the current work path.<p>
	 *
	 * Specific to WebdavPath, not in {@link Path} api
	 * 
	 * @return the current work path
	 * @see WebdavPath#toAbsolutePath()
	 */
	public WebdavPath getCurrentWorkPath() {
		return currentWorkPath;
	}

	/**
	 * Sets the current work path.<p>
	 * 
	 * Makes it an absolute path even if it isn't.<p>
	 * 
	 * Specific to WebdavPath, not in {@link Path} api
	 *
	 * @param currentWorkPath the new current work path
	 * @see WebdavPath#toAbsolutePath()
	 */
	public void setCurrentWorkPath(WebdavPath currentWorkPath) {
		this.currentWorkPath = currentWorkPath;
		this.currentWorkPath.setIsabsolute(true);
	}

	/**
	 * Sets isabsolute.
	 *
	 * Specific to WebdavPath, not in {@link Path} api<p>
	 * not recommended to use this, use those documented in {@link Path} api instead
	 *
	 * @param isabsolute the new isabsolute	 * 
	 */
	public void setIsabsolute(boolean isabsolute) {
		this.isabsolute = isabsolute;
	}	    
    
}
