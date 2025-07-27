package no.maddin.niofs.sftp;

import com.jcraft.jsch.SftpATTRS;

import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SFTPFileAttributes implements PosixFileAttributes {
    private final FileTime lastModifiedTime;
    private final FileTime lastAccessTime;
    private final FileTime creationTime;
    private final boolean isRegularFile;
    private final boolean isDirectory;
    private final boolean isSymbolicLink;
    private final boolean isOther;
    private final long size;
    private final Object fileKey;
    private final String owner;
    private final String group;
    private final Set<PosixFilePermission> permissions;

    SFTPFileAttributes(SftpATTRS stat) {
        this.lastModifiedTime = FileTime.from(stat.getMTime(), TimeUnit.SECONDS);
        this.lastAccessTime = FileTime.from(stat.getATime(), TimeUnit.SECONDS);
        this.creationTime = FileTime.from(stat.getMTime(), TimeUnit.SECONDS);
        this.isRegularFile = stat.isReg();
        this.isDirectory = stat.isDir();
        this.isSymbolicLink = stat.isLink();
        this.isOther = !stat.isReg() && !stat.isDir() && !stat.isLink();
        this.size = stat.getSize();
        this.fileKey = null;
        this.owner = String.valueOf(stat.getUId());
        this.group = String.valueOf(stat.getGId());
        this.permissions = asPermissions(stat.getPermissions());
    }

    private static Set<PosixFilePermission> asPermissions(int permissions) {
        Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
        
        if ((permissions & 0_400) != 0) {
            result.add(PosixFilePermission.OWNER_READ);
        }
        if ((permissions & 0_200) != 0) {
            result.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((permissions & 0_100) != 0) {
            result.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if ((permissions & 0_40) != 0) {
            result.add(PosixFilePermission.GROUP_READ);
        }
        if ((permissions & 0_20) != 0) {
            result.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((permissions & 0_10) != 0) {
            result.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if ((permissions & 0_4) != 0) {
            result.add(PosixFilePermission.OTHERS_READ);
        }
        if ((permissions & 0_2) != 0) {
            result.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((permissions & 0_1) != 0) {
            result.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        
        return result;
    }

    public static Map<String, Object> asMap(SftpATTRS stat) {
        SFTPFileAttributes attr = new SFTPFileAttributes(stat);
        Map<String, Object> map = new HashMap<>();
        map.put("lastModifiedTime", attr.lastModifiedTime);
        map.put("lastAccessTime", attr.lastAccessTime);
        map.put("creationTime", attr.creationTime);
        map.put("isRegularFile", attr.isRegularFile);
        map.put("isDirectory", attr.isDirectory);
        map.put("isSymbolicLink", attr.isSymbolicLink);
        map.put("isOther", attr.isOther);
        map.put("size", attr.size);
        map.put("fileKey", attr.fileKey);
        return map;
    }

    @Override
    public FileTime lastModifiedTime() {
        return this.lastModifiedTime;
    }

    @Override
    public FileTime lastAccessTime() {
        return this.lastAccessTime;
    }

    @Override
    public FileTime creationTime() {
        return this.creationTime;
    }

    @Override
    public boolean isRegularFile() {
        return this.isRegularFile;
    }

    @Override
    public boolean isDirectory() {
        return this.isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return this.isSymbolicLink;
    }

    @Override
    public boolean isOther() {
        return this.isOther;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public Object fileKey() {
        return this.fileKey;
    }

    @Override
    public UserPrincipal owner() {
        return () -> this.owner;
    }

    @Override
    public GroupPrincipal group() {
        return () -> this.group;
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        return this.permissions;
    }
}
