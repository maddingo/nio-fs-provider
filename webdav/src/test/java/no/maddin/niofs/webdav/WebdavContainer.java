package no.maddin.niofs.webdav;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebdavContainer extends GenericContainer<WebdavContainer> {
    private static final int WEBDAV_PORT = 8088;
    private static final int SFTP_PORT = 2022;
    public static final String USERNAME = "user";
    private static final String PASSWORD = "secret";

    public WebdavContainer() {
        super("drakkan/sftpgo:v2.5.5");
        this
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(userString()))
            .withCommand(
                "sftpgo",
                "portable",
                "--directory",
                "/srv/sftpgo",
                "--username",
                USERNAME,
                "--password",
                PASSWORD,
                "--sftpd-port",
                String.valueOf(SFTP_PORT),
                "--webdav-port",
                String.valueOf(WEBDAV_PORT),
                "--permissions",
                "*",
                "--ssh-commands",
                "*",
                "--log-level",
                "debug"
            )
            .withClasspathResourceMapping("/testdata", "/srv/sftpgo", BindMode.READ_WRITE)
            .withClasspathResourceMapping("/sftpgo-config", "/var/lib/sftpgo", BindMode.READ_WRITE)
            .withExposedPorts(WEBDAV_PORT, SFTP_PORT)
            .waitingFor(Wait.forHttp("/").forPort(WEBDAV_PORT).withBasicCredentials(USERNAME, PASSWORD).forStatusCode(207));
        ;
    }

    @NotNull
    private static String userString() {
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            String uid = System.getenv("UID");
            String gid = System.getenv("GID");
            if (uid != null && gid != null) {
                return uid + ":" + gid;
            }
            throw new IllegalArgumentException("Set environment variables UID and GID to the current user");
        }
        Path procSelf = Paths.get("/proc/self");
        try {
            Object uid = Files.getAttribute(procSelf, "unix:uid");
            Object gid = Files.getAttribute(procSelf, "unix:gid");
            return uid + ":" + gid;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getWebdavPort() {
        return getMappedPort(WEBDAV_PORT);
    }

    public String getWebdavUrl() {
        return "webdav://" + USERNAME + ':' + PASSWORD + '@' + getHost() + ":" + getWebdavPort();
    }

}
