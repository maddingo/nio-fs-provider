package no.maddin.niofs.smb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jcifs.smb.SmbFile;

/**
 * Iterates over SMB entries in an SMB directory. 
 */
public class SMBDirectoryStream implements DirectoryStream<Path> {

    private final SMBPath smbFile;
    private final java.nio.file.DirectoryStream.Filter<? super Path> filter;
    private final SMBFileSystemProvider provider;
    private boolean closed;

    public SMBDirectoryStream(SMBFileSystemProvider provider, SMBPath smbFile, java.nio.file.DirectoryStream.Filter<? super Path> filter) {
        this.smbFile = smbFile;
        this.filter = filter;
        this.provider = provider;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public Iterator<Path> iterator() {
        if (closed) {
            throw new IllegalStateException("Already closed");
        }
        try {
            SmbFile[] files = smbFile.getSmbFile().listFiles();
            List<Path> paths = new ArrayList<Path>(files.length);
            for (SmbFile file : files) {
                SMBPath p = new SMBPath(provider, toURI(file));
                if (filter == null || filter.accept(p)) {
                    paths.add(p);
                }
            }
            return paths.iterator();
        } catch(IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@link SmbFile#getURL()} might return URLS that are invalid.
     */
    private URI toURI(SmbFile file) throws URISyntaxException, UnsupportedEncodingException {

        URL url = file.getURL();
        return new URI(url.getProtocol(), url.getAuthority(), url.getPath(), null);
    }
}
