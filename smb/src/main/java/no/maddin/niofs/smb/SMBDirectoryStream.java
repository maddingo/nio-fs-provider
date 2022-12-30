package no.maddin.niofs.smb;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
        return share.getDiskShare().list(smbFile.getSmbPath()).stream()
            .map(finf -> new SMBPath(share, finf.getFileName()))
            .map(Path.class::cast)
            .toList().iterator();
    }
}
