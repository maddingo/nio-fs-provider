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
