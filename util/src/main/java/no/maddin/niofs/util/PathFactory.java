package no.maddin.niofs.util;

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
     * @param uri valid uri for which the PAth should be generated.
     * @param cl if null, the current context class loader is used
     * @return {@link Path} for the given {@link URI}
     * @throws IOException If the loading of the Filesystem provider failed.
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
