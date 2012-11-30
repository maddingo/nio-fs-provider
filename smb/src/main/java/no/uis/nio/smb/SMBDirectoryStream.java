package no.uis.nio.smb;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;

public class SMBDirectoryStream implements DirectoryStream<Path> {

  private final SMBPath smbFile;
  private final java.nio.file.DirectoryStream.Filter<? super Path> filter;
  private final SMBFileSystemProvider provider;
  private boolean closed=false;
  
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
        SMBPath p = new SMBPath(provider, file.getURL().toURI());
        if (filter == null || filter.accept(p)) {
          paths.add(p);
        }
      }
      return paths.iterator();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private SmbFilenameFilter convertFilter(java.nio.file.DirectoryStream.Filter<? super Path> srcFilter) {
    if (srcFilter == null) {
      return null;
    }
    return null;
  }
  
}
