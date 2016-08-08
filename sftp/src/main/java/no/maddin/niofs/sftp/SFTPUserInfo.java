package no.maddin.niofs.sftp;

import com.jcraft.jsch.UserInfo;

/**
 * Provides the password for SFTP.
 */
public class SFTPUserInfo implements UserInfo {

  private final String password;

  public SFTPUserInfo(String password) {
    this.password = password;
  }

  @Override
  public String getPassphrase() {
    return null;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public boolean promptPassword(String message) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public boolean promptPassphrase(String message) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean promptYesNo(String message) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void showMessage(String message) {
    // TODO Auto-generated method stub

  }
}
