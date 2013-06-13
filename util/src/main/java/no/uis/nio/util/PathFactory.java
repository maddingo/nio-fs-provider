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

package no.uis.nio.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.ServiceLoader;

/**
 * Factory methods for creating a Path.  
 */
public final class PathFactory {

  private PathFactory() {
  }
  
  /**
   * Version of {@link Paths#get(URI)} with a class loader.
   * 
   * Loading file system providers that are supplied in a web application fails, because the list of file system providers is initialized 
   * with the application server's classloader.
   *  
   * The method tries first {@link Paths#get(URI)}. If this fails, The service loader tries to load 
   * FileSystemProviders with the given class loader.   
   * 
   * @param uri 
   * @param cl if null, the current context class loader is used 
   * @return {@link Path} for the given {@link URI}
   * @throws IOException 
   */
  public static Path getPath(URI uri, ClassLoader cl) throws IOException {
    
    Path path = null;
    ClassLoader newCl = cl;
    try {
      path = Paths.get(uri);
    } catch (FileSystemNotFoundException ex) {
      if (cl == null) {
        newCl = Thread.currentThread().getContextClassLoader();
      }

      String scheme = uri.getScheme();
      ServiceLoader<FileSystemProvider> sl = ServiceLoader.load(FileSystemProvider.class, newCl);
      for (FileSystemProvider provider: sl) {
          if (scheme.equalsIgnoreCase(provider.getScheme())) {
              return provider.getPath(uri);
          }
      }
    }
    
    return path;
  }
}
