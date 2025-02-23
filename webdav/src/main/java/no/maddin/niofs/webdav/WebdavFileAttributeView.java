package no.maddin.niofs.webdav;

import com.github.sardine.DavAce;
import com.github.sardine.DavAcl;
import com.github.sardine.DavPrincipal;

import java.io.IOException;
import java.nio.file.attribute.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WebdavFileAttributeView implements PosixFileAttributeView {

    private final WebdavPath path;
    private final DavAcl acl;
    private final List<String> principalCollectionSet;
    private final List<DavPrincipal> principals;

    public WebdavFileAttributeView(WebdavPath path, DavAcl acl, List<String> principalCollectionSet, List<DavPrincipal> principals) {
        this.path = path;
        this.acl = acl;
        this.principalCollectionSet = principalCollectionSet;
        this.principals = principals;
    }

    @Override
    public String name() {
        return "posix";
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PosixFileAttributes readAttributes() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
        WebdavFileSystem davFs = (WebdavFileSystem) path.getFileSystem();
        DavAce ace = new DavAce(new DavPrincipal(DavPrincipal.PrincipalType.KEY, "true", DavPrincipal.KEY_AUTHENTICATED));
        davFs.getSardine().setAcl(path.toUri().toString(), Collections.singletonList(ace));
    }

    @Override
    public void setGroup(GroupPrincipal group) throws IOException {
        throw new UnsupportedOperationException();
    }
}
