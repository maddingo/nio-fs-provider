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
    
    final boolean checkclass = true;
    
    private URI makeURI(String path) throws Exception {
    	URI uri = new URI("webdav", "username:password", "anyhost", webdavPort, path, null, null);
    	//URI uri = new URI("file", null, null, 0, path, null, null);
    	return uri;
    }	

    @Test
    public void testgetRoot() throws Exception {
    	Path root = Paths.get(makeURI("/"));
    	assertThat(root.getRoot().toString().equals("/"), is(true));
    	if(checkclass)
    	  assertThat(root, is(instanceOf(WebdavPath.class)));
    }
    
    @Test
    public void testgetParent() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	assertThat(a.getParent().toString().equals("/a/b"), is(true));
    	if(checkclass)
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
    		assertThat(false,is(true));
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
    	
    	if(checkclass) {
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
    	
    	if(checkclass) {
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
    	if(checkclass) {
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
    	if(checkclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(c, is(instanceOf(WebdavPath.class)));
    		assertThat(d, is(instanceOf(WebdavPath.class)));
    		assertThat(e, is(instanceOf(WebdavPath.class)));
    	}
    }
    
    @Test
    public void relativizeresolve() throws Exception {
    	
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
    	
    	assertThat(p.relativize(p.resolve(q)).equals(q), is(true));
    	
    	if(checkclass) {
        	assertThat(root, is(instanceOf(WebdavPath.class)));
        	assertThat(p, is(instanceOf(WebdavPath.class)));
        	assertThat(q, is(instanceOf(WebdavPath.class)));
    	}
    
    }
    
	@Test
	public void teststartendswith() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = Paths.get(makeURI("/a/b"));
    	
    	assertThat(a.startsWith(b), is(true));
    	assertThat(a.startsWith("/a/b"), is(true));
    	    
    	Path root = Paths.get(makeURI("/"));
    	Path c = root.relativize(Paths.get(makeURI("/b/c")));
    	assertThat(c.toString().equals("b/c"), is(true));
    	
    	assertThat(a.endsWith(c), is(true));
    	assertThat(a.endsWith("b/c"), is(true));
    	assertThat(a.endsWith(b), is(false));    	
    	
    	if(checkclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(c, is(instanceOf(WebdavPath.class)));
    	}
	}
	
	@Test
	public void testresolvsib_subpath() throws Exception {
		Path root = Paths.get(makeURI("/"));		
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = root.relativize(Paths.get(makeURI("/sib")));
    	
    	assertThat(a.resolveSibling(b).toString().equals("/a/b/sib"), is(true));
    	assertThat(a.resolveSibling("sib").toString().equals("/a/b/sib"), is(true));
    	
    	assertThat(a.subpath(1, 3).toString().equals("b/c"), is(true));

    	if(checkclass) {
    		assertThat(a, is(instanceOf(WebdavPath.class)));
    		assertThat(b, is(instanceOf(WebdavPath.class)));
    		assertThat(root, is(instanceOf(WebdavPath.class)));
    	}		
	}	
    
}
