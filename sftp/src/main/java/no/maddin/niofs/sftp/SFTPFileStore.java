package no.maddin.niofs.sftp;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.Objects;

public class SFTPFileStore extends FileStore {
    private final SFTPHost host;

    public SFTPFileStore(SFTPHost host) {
        this.host = host;
    }

    @Override
    public String name() {
        return host.toString();
    }

    @Override
    public String type() {
        return "sftp";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return type == SFTPFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        return "posix".equals(name);
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return null;
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        switch (attribute) {
            case "totalSpace":
                return getTotalSpace();
            case "usableSpace":
                return getUsableSpace();
            case "unallocatedSpace":
                return getUnallocatedSpace();
            default:
                throw new UnsupportedOperationException("Attribute '" + attribute + "' not supported");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SFTPFileStore)) {
            return false;
        }
        SFTPFileStore other = (SFTPFileStore) obj;
        return Objects.equals(host, other.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host);
    }
}