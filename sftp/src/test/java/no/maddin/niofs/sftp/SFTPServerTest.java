package no.maddin.niofs.sftp;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * Tests that require a running SSHD server.
 */
public class SFTPServerTest {

    private SshServer sshd;
    private int port;

    /**
     * https://mina.apache.org/sshd-project/embedding_ssh.html
     */
    @Before
    public void setupSftpServer() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            sshd = SshServer.setUpDefaultServer();
            this.port = serverSocket.getLocalPort();
            serverSocket.close();
            sshd.setPort(this.port);
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("target", "hostkey.ser")));

            sshd.setShellFactory(new ProcessShellFactory(new String[]{"/bin/sh", "-i", "-l"}));
            sshd.setCommandFactory(new ScpCommandFactory());
            sshd.start();
        }
    }

    @After
    public void stopServer() throws Exception {
        sshd.stop();
    }

    @Test
    public void createDirectories() throws Exception {
        URI uri = new URI("sftp", "test", "localhost", port, "/a/b/", null, null);
        Path path = Paths.get(uri);
        Path newPath = Files.createDirectories(path);
        assertThat(newPath, is(notNullValue()));
//        File fileA = new File(rootFolder.getAbsolutePath(), "a");
//        assertThat(fileA.exists(), is(true));
//        assertThat(fileA.isDirectory(), is(true));
//        File fileB = new File(fileA.getAbsolutePath(), "b");
//        assertThat(fileB.exists(), is(true));
//        assertThat(fileB.isDirectory(), is(true));

    }
}
