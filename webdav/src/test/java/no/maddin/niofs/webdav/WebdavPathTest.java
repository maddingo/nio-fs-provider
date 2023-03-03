package no.maddin.niofs.webdav;
//CHECKSTYLE:OFF

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * These are the tests that don't require a running server.
 */
public class WebdavPathTest {

    private int webdavPort = -1;

    @Test
    public void newFileSystemWebdav() throws Exception {
        URI uri = new URI("webdav", "user:password","localhost", webdavPort, "/", null, null);

        FileSystem fs = FileSystems.newFileSystem(uri, null);

        assertThat(fs, is(notNullValue()));
    }

    @Test
    public void newFileSystemWebdavs() throws Exception {
        URI uri = new URI("webdavs", "user:password","localhost", webdavPort, "/", null, null);

        FileSystem fs = FileSystems.newFileSystem(uri, null);

        assertThat(fs, is(notNullValue()));
    }

    @Test
    public void getURI() throws Exception {
        URI uri = new URI("webdav", "user:password","localhost", webdavPort, "/", null, null);

        Path path = Paths.get(uri);

        assertThat(path, is(notNullValue()));
    }

    @Test
    public void normalize() throws Exception {
        String dottedPath = "/webdav/../test/something";

        URI uri = new URI("webdav", "username:password", "anyhost", webdavPort, dottedPath, null, null);

        Path path = Paths.get(uri);
        Path result = path.normalize();

        assertThat(result, is(instanceOf(WebdavPath.class)));

        String resultUri = result.toUri().toString();
        assertThat(resultUri, not(containsString("..")));
        assertThat(result.isAbsolute(), is(true));
    }
    
    /*
     * to swap tests between WepdavPath and UnixPath or some other java internal file:// path
     * 
     * set testwebdavclass to true for the normal WebdavPath test
     * 
     * set testwebdavclass to false for the alternate test
     * 
     * the Junit test runs would show the differences between the two
     * 
     */
    final boolean testwebdavclass = true;
    
    private URI makeURI(String path) throws Exception {
    	URI uri;
    	if(testwebdavclass) {
    		uri = new URI("webdav", "username:password", "anyhost", webdavPort, path, null, null);
    	} else {
    		uri = new URI("file", null, null, 0, path, null, null);
    	}
    	return uri;
    }	

    @Test
    public void testgetRoot() throws Exception {
    	Path root = Paths.get(makeURI("/"));
    	assertThat(root.getRoot().toString().equals("/"), is(true));
    	if(testwebdavclass)
    	  assertThat(root, is(instanceOf(WebdavPath.class)));
    	
    	Path endslash = Paths.get(makeURI("/aaa/"));
    	assertThat(endslash.toString().equals("/aaa"),is(true)); 
    	
    }
    
    @Test
    public void testgetParent() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	assertThat(a.getParent().toString().equals("/a/b"), is(true));
    	if(testwebdavclass)
    	  assertThat(a, is(instanceOf(WebdavPath.class)));    	
    }
    
    @Test 
    public void testiterator_getname() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	
    	assertThat(a.getFileName().toString().equals("c"), is(true));
    	
    	Path root = Paths.get(makeURI("/"));
    	assertThat(root.getFileName(), nullValue());
    	
    	try {
    		root.getName(0);
    		assertThat(false,is(true)); //shouldn't reach here
    	} catch (IllegalArgumentException e) {
    		//Logger log = Logger.getLogger("testok");
    		//log.info("getName empty elements passed");
    	}    	
    	
    	int n = a.getNameCount();
    	assertThat(n,equalTo(3));
    	    	
    	Iterator<Path> iter = a.iterator();
    	int i = 0;  
    	Path b = null;
    	while(iter.hasNext()) {
    		b = iter.next();
    		assertThat(a.getName(i).toString().equals(b.getFileName().toString()), is(true));
    		i++;
    	}
    	
    	if(testwebdavclass) {
      	  	assertThat(a, is(instanceOf(WebdavPath.class)));
      	  	assertThat(b, is(instanceOf(WebdavPath.class)));
      	  	assertThat(root, is(instanceOf(WebdavPath.class)));
    	}
    }
    
    @Test
    public void testtoAbsPath() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	assertThat(a.toAbsolutePath().toString().equals("/a/b/c"), is(true));
    	
    	Path root = Paths.get(makeURI("/"));
    	Path b = root.relativize(a); 
    	assertThat(b.toString().equals("a/b/c"), is(true));
    	assertThat(b.toAbsolutePath().toString().equals("/a/b/c"), is(true));
    	
    	if(testwebdavclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));    	
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(root, is(instanceOf(WebdavPath.class)));
    	}
    }
    
    @Test
    public void testequals() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = Paths.get(makeURI("/a/b/e"));
    	Path c = Paths.get(makeURI("/a/b/c/e"));
    	Path d = Paths.get(makeURI("/a/b"));
    	Path e = Paths.get(makeURI("/a/b/c"));
    	
    	assertThat(a.equals(b), is(false));
    	assertThat(a.equals(c), is(false));
    	assertThat(a.equals(d), is(false));
    	assertThat(a.equals(e), is(true));
    	if(testwebdavclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(c, is(instanceOf(WebdavPath.class)));
    		assertThat(d, is(instanceOf(WebdavPath.class)));
    		assertThat(e, is(instanceOf(WebdavPath.class)));
    	}
    }
    
    @Test
    public void testcompareTo() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = Paths.get(makeURI("/a/b/e"));
    	Path c = Paths.get(makeURI("/a/b/c/e"));
    	Path d = Paths.get(makeURI("/a/b"));
    	Path e = Paths.get(makeURI("/a/b/c"));
    	
    	assertThat(a.compareTo(b) < 0, is(true));
    	assertThat(a.compareTo(c) < 0, is(true));
    	assertThat(a.compareTo(d) > 0, is(true));
    	assertThat(a.compareTo(e) == 0, is(true));
    	    	    	
    	if(testwebdavclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(c, is(instanceOf(WebdavPath.class)));
    		assertThat(d, is(instanceOf(WebdavPath.class)));
    		assertThat(e, is(instanceOf(WebdavPath.class)));
    		
        	try {
        		Path u = Paths.get(new URI("file:///")); // UnixPath
        		a.compareTo(u);
        		assertThat(false, is(true)); //shouldn't reach here
        	} catch (ClassCastException e1) {
        		//Logger log = Logger.getLogger("test");
        		//log.info(e1.getMessage()); //test ok
        	}
    	}
    }
    
    @Test
    public void testrelativizeresolve() throws Exception {
    	
    	Path root = Paths.get(makeURI("/"));
    	assertThat(root.toString().equals("/"), is(true));
    
    	String ps = "/a/b";
    	Path p = Paths.get(makeURI(ps));
    	assertThat(p.toString().equals("/a/b"), is(true));
    	
    	String qs = "/c/d";
    	Path qa =Paths.get(makeURI(qs));;
    	assertThat(qa.toString().equals("/c/d"), is(true));
    	
    	Path q = root.relativize(qa);  //gets relative path "c/d"
    	assertThat(q.toString().equals("c/d"), is(true));
    	
    	assertThat(p.resolve(q).toString().equals("/a/b/c/d"), is(true));    	    	
    	
    	assertThat(p.resolve(q).equals(p.resolve("c/d")), is(true));
    	    	
    	Logger log = Logger.getLogger("test");
    	// "/a/b".relativize("a/b/c/d") returns "c/d"
    	assertThat(p.relativize(p.resolve(q)).equals(q), is(true));
    	//log.info(p.relativize(p.resolve(q)).toString());    	
    	
    	assertThat(p.resolve(qa).equals(qa), is(true));
    	
    	assertThat(p.resolve(root).equals(root), is(true));
    	
    	Path empty = root.relativize(root);
    	assertThat(p.resolve(empty).equals(p), is(true));    	
    	
    	//check exception conditions    	    	      	    	    	    	

    	if(testwebdavclass) {
        	assertThat(root, is(instanceOf(WebdavPath.class)));
        	assertThat(empty, is(instanceOf(WebdavPath.class)));
        	assertThat(p, is(instanceOf(WebdavPath.class)));
        	assertThat(q, is(instanceOf(WebdavPath.class)));        	
        	
        	try {
        		Path u = Paths.get(new URI("file:///")); // UnixPath
        		p.relativize(u);
        		assertThat(false, is(true)); //shouldn't reach here
        	} catch (IllegalArgumentException e) {    		
        		//log.info(e.getMessage()); //test ok
        	}

        	try {
        		Path u = Paths.get(new URI("file:///")); // UnixPath    		
        		p.resolve(u.relativize(u.getRoot()));
        		assertThat(false, is(true)); //shouldn't reach here
        	} catch (IllegalArgumentException e) {    		
        		//log.info(e.getMessage()); //test ok
        	}

    	} 

    	try {    		
    		p.resolve((Path)null);
    		assertThat(false, is(true)); //shouldn't reach here
    	} catch (NullPointerException e) {    		
    		//log.info(e.getMessage()); //test ok
    	}

    	try {    		
    		p.relativize((Path)null);
    		assertThat(false, is(true)); //shouldn't reach here
    	} catch (NullPointerException e) {    		
    		//log.info(e.getMessage()); //test ok
    	}
    	
    	try {
    		/*
    		 * There is a difference between WebdavPath implementation vs
    		 * UnixPath implementation, UnixPath resolves this successfully
    		 * 
    		 * "/a/b/c/d".relativize("/a/b/e/f") returns "../../e/f"
    		 *
    		 * while this returns IllegalArgumentException
    		 *
    		 * current implementation of relativivize requires the 
    		 * current path to start with and is a subset of the other path
    		 * 
    		 * e.g. ""/a/b".relativize("/a/b/c/d") returns "c/d"
    		 * 
    		 * i.e. the current WebdavPath.relativize() returns the remaining subpath as a
    		 * relative path. This results conforms to JDK 1.8 javadoc spec
    		 * 
    		 */
        	Path r = Paths.get(makeURI("/a/b/e/f"));
    		log.info(p.resolve(q).relativize(r).toString()); //"/a/b/c/d".relativize("/a/b/e/f")
    		if(testwebdavclass) assertThat(false, is(true)); //shouldn't reach here
    	} catch (IllegalArgumentException e) {
    		//log.info("test ok: exception received:".concat(e.getMessage())); 
    	}
    	
    	try {
    		/*
    		 * relativize a shorter path against a longer path
    		 * 
 			 * There is a difference between WebdavPath implementation vs
    		 * UnixPath implementation, UnixPath resolves this successfully
    		 * 
    		 * "/a/b/c/d".relativize("/a/b") returns "../.."
    		 * 
    		 * while this returns 
    		 */
    		log.info(p.resolve(q).relativize(p).toString()); // "/a/b/c/d".relativize("/a/b")
    		if(testwebdavclass) assertThat(false, is(true)); //shouldn't reach here
    	} catch (IllegalArgumentException e) {
    		//log.info("test ok: exception received:".concat(e.getMessage())); 
    	}
    	
    	try {
    		/*
    		 * Relativize a relative path against absolute path.    		 * 
    		 * 
    		 * A relative path cannot be constructed if only one of the paths have a root component.
    		 *  
    		 * This test returns the same exception for both WebdavPath and UnixPath
    		 * conforms to javadoc spec for jdk 1.8
    		 * 
    		 */
    		log.info(p.resolve(q).relativize(q).toString()); // "/a/b/c/d".relativize("c/d")
    		assertThat(false, is(true)); //shouldn't reach here
    	} catch (IllegalArgumentException e) {
    		//log.info("test ok: exception received:".concat(e.getMessage())); 
    	}

    
    }
    
	@Test
	public void teststartendswith() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = Paths.get(makeURI("/a/b"));
    	Path e = Paths.get(makeURI("/a/e"));
    	
    	assertThat(a.startsWith(b), is(true));
    	assertThat(a.startsWith("/a/b"), is(true));
    	assertThat(b.startsWith(a), is(false));
    	assertThat(a.startsWith(e), is(false));

    	Path c = Paths.get(new URI("webdav", "username:password", "anotherhost", webdavPort, "/a/b", null, null));
    	assertThat(a.startsWith(c), is(false));
    	
    	Path root = Paths.get(makeURI("/"));
    	Path d = root.relativize(Paths.get(makeURI("/b/c")));
    	assertThat(d.toString().equals("b/c"), is(true));
    	
    	assertThat(a.endsWith(d), is(true));
    	assertThat(a.endsWith("b/c"), is(true));
    	assertThat(a.endsWith(b), is(false));
    	assertThat(a.endsWith(c), is(false));
    	    	
    	
    	if(testwebdavclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(c, is(instanceOf(WebdavPath.class)));
    		assertThat(d, is(instanceOf(WebdavPath.class)));
    		assertThat(e, is(instanceOf(WebdavPath.class)));
    		
        	Path u = Paths.get(new URI("file:///a/b")); // UnixPath
        	assertThat(a.startsWith(u), is(false));
        	
        	assertThat(a.endsWith(u), is(false));
    	}
	}
	
	@Test
	public void testresolvsib_subpath() throws Exception {
		Path root = Paths.get(makeURI("/"));		
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = root.relativize(Paths.get(makeURI("/sib")));
    	
    	assertThat(a.resolveSibling(b).toString().equals("/a/b/sib"), is(true));
    	assertThat(a.resolveSibling("sib").toString().equals("/a/b/sib"), is(true));

    	Path empty = root.relativize(root); //makes an empty path
    	assertThat(empty.resolveSibling(b).toString().equals("sib"), is(true));		
    	    	
    	assertThat(a.subpath(1, 3).toString().equals("b/c"), is(true));
    	    	
    	try {
        	a.subpath(1, 4);
    		assertThat(false, is(true)); //shouldn't reach here
    	} catch (IllegalArgumentException e) {
    	}

    	try {
    		root.subpath(0, 1);
    		assertThat(false, is(true)); //shouldn't reach here
    	} catch (IllegalArgumentException e) {
    	}

    	if(testwebdavclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(root, is(instanceOf(WebdavPath.class)));
    		
        	try {
        		Path u = Paths.get(new URI("file:///")); // UnixPath    		
        		a.resolveSibling(u);
        		assertThat(false, is(true)); //shouldn't reach here
        	} catch (IllegalArgumentException e) { 
        		//Logger log = Logger.getLogger("test");
        		//log.info(e.getMessage()); //test ok
        	}
    	}		
	}	
	    
}
