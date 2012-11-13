package no.uis.nio.commons;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.ServiceLoader;

public class PathFactory {

  /**
   * Version of {@link Paths#get(URI)} with a class loader.
   * 
   * @param uri 
   * @param cl if null, the current context class loader is used 
   * @return {@link Path} for the given {@link URI}
   * @throws IOException 
   */
  public static Path getPath(URI uri, ClassLoader cl) throws IOException {
    
    Path path = null;
    try {
      path = Paths.get(uri);
    } catch (FileSystemNotFoundException ex) {
      if (cl == null) {
        cl = Thread.currentThread().getContextClassLoader();
      }

      String scheme = uri.getScheme();
      ServiceLoader<FileSystemProvider> sl = ServiceLoader
          .load(FileSystemProvider.class, cl);
      for (FileSystemProvider provider: sl) {
          if (scheme.equalsIgnoreCase(provider.getScheme())) {
              return provider.getPath(uri);
          }
      }
    }
    
    return path;
  }
}
