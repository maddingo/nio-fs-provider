package no.maddin.niofs.smb;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Iterates over SMB entries in an SMB directory. 
 */
public class SMBDirectoryStream implements DirectoryStream<Path> {

    private final SMBPath smbFile;
    private final java.nio.file.DirectoryStream.Filter<? super Path> filter;
    private final SMBFileSystemProvider provider;
    private final AtomicBoolean iteratorReturned = new AtomicBoolean();

    SMBDirectoryStream(SMBFileSystemProvider provider, SMBPath smbFile, java.nio.file.DirectoryStream.Filter<? super Path> filter) {
        this.smbFile = smbFile;
        this.filter = filter;
        this.provider = provider;
    }

    @Override
    public void close() throws IOException {
        smbFile.getShare().close();
    }

    @Override
    public Iterator<Path> iterator() {
        SMBShare share = smbFile.getShare();
        List<FileIdBothDirectoryInformation> list = share.getDiskShare().list(smbFile.getSmbPath());
        return list.stream()
            .map(finf -> new SMBPath(share, smbFile.getSmbPath() + share.getSeparator() + finf.getFileName()))
            .map(Path.class::cast)
            .filter(this::accept)
            .collect(Collectors.toList())
            .iterator();
    }

    /**
     * handle null filter and IOException
     */
    private boolean accept(Path path) {
        try {
            return filter == null || filter.accept(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
