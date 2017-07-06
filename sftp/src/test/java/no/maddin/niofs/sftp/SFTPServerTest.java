package no.maddin.niofs.sftp;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystem;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * Tests that require a running SSHD server.
 */
public class SFTPServerTest {

    private SshServer sshd;
    private int port;
    private String sftpUserame = "username";
    private String sftpPassword = "password";

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
            sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
                @Override
                public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException {
                    return sftpUserame.equals(username) && sftpPassword.equals(password);
                }
            });

            sshd.setShellFactory(new ProcessShellFactory("/bin/sh", "-i", "-l"));
            sshd.setSubsystemFactories(Collections.<NamedFactory<Command>>singletonList(new SftpSubsystemFactory()));
            sshd.setFileSystemFactory(new VirtualFileSystemFactory(Paths.get(System.getProperty("user.dir"), "target")));

            sshd.setCommandFactory(new ScpCommandFactory());
            sshd.start();
        }
    }

    @After
    public void stopServer() throws Exception {
        sshd.stop();
    }

    @Ignore
    @Test
    public void createDirectories() throws Exception {
        URI uri = new URI("sftp", sftpUserame + ':' + sftpPassword, "localhost", port, "/~/a/b/", null, null);
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
