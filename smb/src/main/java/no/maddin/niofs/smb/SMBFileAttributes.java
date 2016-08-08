package no.maddin.niofs.smb;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import jcifs.smb.SmbFile;

/**
 * File attributes for SMB files and directories.
 */
public class SMBFileAttributes implements BasicFileAttributes {

  private final int attributes;
  public SMBFileAttributes(String uncPath, int attributes) {
    this.attributes = attributes;
  }

  @Override
  public FileTime lastModifiedTime() {
    return null;
  }

  @Override
  public FileTime lastAccessTime() {
    return null;
  }

  @Override
  public FileTime creationTime() {
    return null;
  }

  @Override
  public boolean isRegularFile() {
    return !isDirectory();
  }

  @Override
  public boolean isDirectory() {
    return (attributes & SmbFile.ATTR_DIRECTORY) != 0;
  }

  @Override
  public boolean isSymbolicLink() {
    return false;
  }

  @Override
  public boolean isOther() {
    return false;
  }

  @Override
  public long size() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Object fileKey() {
    return null;
  }
}
