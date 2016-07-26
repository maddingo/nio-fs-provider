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

package no.uis.nio.smb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jcifs.smb.SmbFile;

/**
 * Iterates over SMB entries in an SMB directory. 
 */
public class SMBDirectoryStream implements DirectoryStream<Path> {

    private final SMBPath smbFile;
    private final java.nio.file.DirectoryStream.Filter<? super Path> filter;
    private final SMBFileSystemProvider provider;
    private boolean closed;

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
                SMBPath p = new SMBPath(provider, toURI(file));
                if (filter == null || filter.accept(p)) {
                    paths.add(p);
                }
            }
            return paths.iterator();
        } catch(IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * file.getURL() might return URLS that are invalid
     */
    private URI toURI(SmbFile file) throws URISyntaxException, UnsupportedEncodingException {

        URL url = file.getURL();
        StringBuilder sb = new StringBuilder("smb://");
        sb.append(url.getAuthority());
        String path = url.getPath();
        for (String p : path.split("/")) {
            if (!p.isEmpty()) {
                sb.append("/");
                sb.append(encode(p));
            }
        }

        return new URI(sb.toString());
    }

    public static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

}
