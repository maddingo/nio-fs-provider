package no.maddin.niofs.webdav;
//CHECKSTYLE:OFF

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Logger;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * These are the tests that don't require a running server.
 */
class WebdavPathTest {

    private int webdavPort = -1;

    @Test
	void newFileSystemWebdav() throws Exception {
        URI uri = new URI("webdav", "user:password","localhost", webdavPort, "/", null, null);

        FileSystem fs = FileSystems.newFileSystem(uri, null);

        assertThat(fs, is(notNullValue()));
    }

    @Test
	void newFileSystemWebdavs() throws Exception {
        URI uri = new URI("webdavs", "user:password","localhost", webdavPort, "/", null, null);

        FileSystem fs = FileSystems.newFileSystem(uri, null);

        assertThat(fs, is(notNullValue()));
    }

    @Test
	void getURI() throws Exception {
        URI uri = new URI("webdav", "user:password","localhost", webdavPort, "/", null, null);

        Path path = Paths.get(uri);

        assertThat(path, is(notNullValue()));
    }

    @Test
	void normalize() throws Exception {
        String dottedPath = "/webdav/../test/something";

        URI uri = new URI("webdav", "username:password", "anyhost", webdavPort, dottedPath, null, null);

        Path path = Paths.get(uri);
        Path result = path.normalize();

        assertThat(result, is(instanceOf(WebdavPath.class)));

        String resultUri = result.toUri().toString();
        assertThat(resultUri, not(containsString("..")));
        assertThat(result.isAbsolute(), is(true));
    }
    
    private URI makeURI(String path) throws Exception {
        return new URI("webdav", "username:password", "anyhost", webdavPort, path, null, null);
    }	

    @Test
	void getParent() throws Exception {
    	    	
    	Path a = Paths.get(makeURI("/a/b/c"));
    	assertThat(a.getParent().toString().equals("/a/b"), is(true));
    	
    	Path root = Paths.get(makeURI("/"));
    	
    	assertThat(root.getParent(), is(nullValue()));
    	
    	Path aaa = Paths.get(makeURI("/aaa"));
    	assertThat(aaa.getParent().toString(), is("/"));
    	
    	Path relpath = root.relativize(aaa);
    	assertThat(relpath.getRoot(), is(nullValue()));

		assertThat(root, is(instanceOf(WebdavPath.class)));
		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(aaa, is(instanceOf(WebdavPath.class)));
		assertThat(relpath, is(instanceOf(WebdavPath.class)));
    }

    @Test
	void toAbsPath() throws Exception {
    	
    	// absolute paths simply return itself
    	Path a = Paths.get(makeURI("/a/b/c"));
    	assertThat(a.toAbsolutePath().toString(), equalTo("/a/b/c"));
    	Path root = Paths.get(makeURI("/"));
    	
    	//relative paths is resolved
    	Path base = Paths.get(makeURI("/currentWorkPath"));
    	Path relpath = base.relativize(base.resolve("relativepath")); 
    	assertThat(relpath.toString(), equalTo("relativepath"));
		//relativize preserve current workpath
		//relpath.toAbsolutePath() recovers original path
		assertThrows(UnsupportedOperationException.class, () -> {
			relpath.toAbsolutePath().toString();
		});
		//getFileName preserve current workpath
		//relpath.getFilename().toAbsolutePath() recovers original path
		assertThrows(UnsupportedOperationException.class, () -> {
			a.getFileName().toAbsolutePath().toString();
		});
		//getName(index) preserve current workpath
		//relpath.getName(index).toAbsolutePath() recovers original path
		assertThrows(UnsupportedOperationException.class, () -> {
			a.getName(1).toAbsolutePath().toString();
		});

		//relative path formed from FileSYstem.getPath do not have a current working path
		//fspath.toAbsolutePath() is resolved against default root
		Path fspath = a.getFileSystem().getPath("test");
		assertThat(fspath.toString(), equalTo("test"));
		assertThrows(UnsupportedOperationException.class, () -> {
			fspath.toAbsolutePath().toString();
		});

		//if the base path is a relative path
		//current work path is lost and hence toAbsolutePath() resolve against default root
		Path rabc = root.relativize(a);
		assertThat(rabc.toString(), equalTo("a/b/c"));
		Path rab = root.relativize(a.getParent());
		assertThat(rab.toString(), equalTo("a/b"));
		assertThrows(UnsupportedOperationException.class, () -> {
			rab.relativize(rabc).toAbsolutePath().toString();
		});

		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(relpath, is(instanceOf(WebdavPath.class)));
		assertThat(root, is(instanceOf(WebdavPath.class)));
	}
    
    @Test
	void relativizeRoot() throws Exception {
    	
    	// make a relative path
    	Path root = Paths.get(makeURI("/"));
    	assertThat(root.getRoot().toString().equals("/"), is(true));

    	Path aaa = Paths.get(makeURI("/aaa"));
    	Path relpath = root.relativize(aaa);
    	assertThat(relpath.getRoot(), is(nullValue()));
    	assertThat (relpath.toString(), equalTo("aaa"));
    	assertThat(root.getRoot().toString().equals("/"), is(true));

		assertThat(root, is(instanceOf(WebdavPath.class)));
		assertThat(aaa, is(instanceOf(WebdavPath.class)));
		assertThat(relpath, is(instanceOf(WebdavPath.class)));
	}

	@Test
	void names() throws Exception {
		
    	Path root = Paths.get(makeURI("/"));
    	assertThat(root.getFileName(), is(nullValue()));
		assertThrows(IndexOutOfBoundsException.class, () -> {
			root.getName(0).toString();
		});

    	Path empty = root.relativize(root);
    	assertThat(empty.toString(), equalTo(""));
    	assertThat(empty.getFileName(), is(nullValue()));
		assertThrows(UnsupportedOperationException.class, () -> {
			empty.getName(0).toString();
		});

    	Path a = Paths.get(makeURI("/a"));
    	assertThat(a.toString(), equalTo("/a"));
    	assertThat(a.getFileName().toString(), equalTo("a"));
    	assertThat(a.getName(0).toString(), equalTo("a"));

    	Path relpath = root.relativize(a);
    	assertThat(relpath.toString(), equalTo("a"));
    	assertThat(relpath.getFileName().toString(), equalTo("a"));
    	assertThrows(UnsupportedOperationException.class, () -> relpath.getName(0).toString());
    	
    	Path ab = Paths.get(makeURI("/a/b"));
    	assertThat(ab.toString(), equalTo("/a/b"));
    	assertThat(ab.getFileName().toString(), equalTo("b"));
    	assertThat(ab.getName(1).toString(), equalTo("b"));
    	assertThat(ab.getName(0).toString(), equalTo("a"));

    	Path relab = root.relativize(ab);
    	assertThat(relab.toString(), equalTo("a/b"));
    	assertThat(relab.getFileName().toString(), equalTo("b"));

		assertThrows(UnsupportedOperationException.class, () -> {
			relab.getName(2);
		});

		assertThrows(IndexOutOfBoundsException.class, () -> {
			root.getName(0);
		});
		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(ab, is(instanceOf(WebdavPath.class)));
		assertThat(relpath, is(instanceOf(WebdavPath.class)));
		assertThat(relab, is(instanceOf(WebdavPath.class)));
		assertThat(root, is(instanceOf(WebdavPath.class)));
	}

    @Test
	void iteratorGetName() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	assertThat(a.toString(), equalTo("/a/b/c"));
    	assertThat(a.getFileName().toString().equals("c"), is(true));
    	    	
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
    	
    	Path root = Paths.get(makeURI("/"));
    	Path relpath = root.relativize(a);
    	assertThat(relpath.toString(), equalTo("a/b/c"));
    	i = 0;  
    	b = null;
    	iter = relpath.iterator();
    	while(iter.hasNext()) {
    		b = iter.next();
    		assertThat(a.getName(i).toString().equals(b.getFileName().toString()), is(true));
    		i++;
    	}
    	
		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(b, is(instanceOf(WebdavPath.class)));
    }
        
    @Test
	void pathEquals() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = Paths.get(makeURI("/a/b/e"));
    	Path c = Paths.get(makeURI("/a/b/c/e"));
    	Path d = Paths.get(makeURI("/a/b"));
    	Path e = Paths.get(makeURI("/a/b/c"));
    	
    	assertThat(a.equals(b), is(false));
    	assertThat(a.equals(c), is(false));
    	assertThat(a.equals(d), is(false));
    	assertThat(a.equals(e), is(true));
		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(b, is(instanceOf(WebdavPath.class)));
		assertThat(c, is(instanceOf(WebdavPath.class)));
		assertThat(d, is(instanceOf(WebdavPath.class)));
		assertThat(e, is(instanceOf(WebdavPath.class)));
    }
    
    @Test
	void compareTo() throws Exception {
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = Paths.get(makeURI("/a/b/e"));
    	Path c = Paths.get(makeURI("/a/b/c/e"));
    	Path d = Paths.get(makeURI("/a/b"));
    	Path e = Paths.get(makeURI("/a/b/c"));

    	assertThat(a.compareTo(b), is(lessThan(0)));
    	assertThat(a.compareTo(c), is(lessThan(0)));
    	assertThat(a.compareTo(d), is(greaterThan(0)));
    	assertThat(a.compareTo(e), is(equalTo(0)));
    	    	    	
		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(b, is(instanceOf(WebdavPath.class)));
		assertThat(c, is(instanceOf(WebdavPath.class)));
		assertThat(d, is(instanceOf(WebdavPath.class)));
		assertThat(e, is(instanceOf(WebdavPath.class)));

		assertThrows(ClassCastException.class, () -> {
			Path u = Paths.get(new URI("file:///")); // UnixPath
			a.compareTo(u);
		});
    }
    
    @Test
	void relativizeResolve() throws Exception {
    	
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
    	    	
    	// "/a/b".relativize("a/b/c/d") returns "c/d"
    	assertThat(p.relativize(p.resolve(q)).equals(q), is(true));

    	assertThat(p.resolve(qa).equals(qa), is(true));
    	
    	assertThat(p.resolve(root).equals(root), is(true));
    	
    	Path empty = root.relativize(root);
    	assertThat(p.resolve(empty).equals(p), is(true));    	
    	
    	//check exception conditions    	    	      	    	    	    	
		assertThat(root, is(instanceOf(WebdavPath.class)));
		assertThat(empty, is(instanceOf(WebdavPath.class)));
		assertThat(p, is(instanceOf(WebdavPath.class)));
		assertThat(q, is(instanceOf(WebdavPath.class)));

		assertThrows(IllegalArgumentException.class, () -> {
			Path u = Paths.get(new URI("file:///")); // UnixPath
			p.relativize(u);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			Path u = Paths.get(new URI("file:///")); // UnixPath
			p.resolve(u.relativize(u.getRoot()));
		});

		assertThrows(IllegalArgumentException.class, () -> {
			p.resolve((Path)null);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			p.relativize((Path)null);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			Path r = Paths.get(makeURI("/a/b/e/f"));
			//"/a/b/c/d".relativize("/a/b/e/f")
			p.resolve(q).relativize(r).toString();

			assertThat(false, is(true)); //shouldn't reach here
		});

		assertThrows(IllegalArgumentException.class, () -> {
			// "/a/b/c/d".relativize("/a/b")
			p.resolve(q).relativize(p).toString();
			assertThat(false, is(true)); //shouldn't reach here
		});

		assertThrows(IllegalArgumentException.class, () -> {
			// "/a/b/c/d".relativize("c/d")
			p.resolve(q).relativize(q).toString();
		});
    }
    
	@Test
	void startEndsWith() throws Exception {
		Path root = Paths.get(makeURI("/"));
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = Paths.get(makeURI("/a/b"));
    	Path e = Paths.get(makeURI("/a/e"));
    	
    	assertThat(a.startsWith(b), is(true));
    	assertThat(a.startsWith("/a/b"), is(true));
    	assertThat(b.startsWith(a), is(false));
    	assertThat(a.startsWith(e), is(false));
    	assertThat(b.startsWith(root.relativize(b)), is(false));

    	Path c = Paths.get(new URI("webdav", "username:password", "anotherhost", webdavPort, "/a/b", null, null));
    	assertThat(a.startsWith(c), is(false));
    	
    	Path d = root.relativize(Paths.get(makeURI("/b/c")));
    	assertThat(d.toString().equals("b/c"), is(true));
    	
    	assertThat(a.endsWith(d), is(true));
    	assertThat(a.endsWith("b/c"), is(true));
    	assertThat(a.endsWith(b), is(false));
    	assertThat(a.endsWith(c), is(false));
    	assertThat(b.endsWith(root.relativize(a)), is(false));
    	assertThat(d.endsWith(b), is(false));
    	assertThat(b.endsWith(root.relativize(e)), is(false));
    	    	
		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(b, is(instanceOf(WebdavPath.class)));
		assertThat(c, is(instanceOf(WebdavPath.class)));
		assertThat(d, is(instanceOf(WebdavPath.class)));
		assertThat(e, is(instanceOf(WebdavPath.class)));
    		
		Path u = Paths.get(new URI("file:///a/b")); // UnixPath
		assertThat(a.startsWith(u), is(false));
        	
		assertThat(a.endsWith(u), is(false));
	}
	
	@Test
	void resolveSubPath() throws Exception {
		Path root = Paths.get(makeURI("/"));		
    	Path a = Paths.get(makeURI("/a/b/c"));
    	Path b = root.relativize(Paths.get(makeURI("/sib")));
    	
    	assertThat(a.resolveSibling(b).toString().equals("/a/b/sib"), is(true));
    	assertThat(a.resolveSibling("sib").toString().equals("/a/b/sib"), is(true));

    	Path empty = root.relativize(root); //makes an empty path
    	assertThat(empty.resolveSibling(b).toString().equals("sib"), is(true));
    	
    	Path c = Paths.get(makeURI("/c"));
    	assertThat(c.resolveSibling(b).toString().equals("/sib"), is(true));
    	
    	assertThat(root.resolveSibling(b).toString().equals("sib"), is(true));
    	
    	assertThat(a.subpath(1, 3).toString().equals("b/c"), is(true));

		assertThrows(IllegalArgumentException.class, () -> {
			a.subpath(1, 4);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			root.subpath(0, 1);
		});

		assertThat(a, is(instanceOf(WebdavPath.class)));
		assertThat(b, is(instanceOf(WebdavPath.class)));
		assertThat(c, is(instanceOf(WebdavPath.class)));
		assertThat(root, is(instanceOf(WebdavPath.class)));

		assertThrows(IllegalArgumentException.class, () -> {
			Path u = Paths.get(new URI("file:///")); // UnixPath
			a.resolveSibling(u);
		});
	}
}
