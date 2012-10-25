package no.uis.nio.webdav;

public class WebdavsFileSystemProvider extends WebdavFileSystemProvider {

  @Override
  public String getScheme() {
    return "webdavs";
  }

}
