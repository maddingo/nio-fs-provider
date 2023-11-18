package no.maddin.niofs.webdav;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class WebdavContainer extends GenericContainer<WebdavContainer> {
    private static final int WEBDAV_PORT = 8088;
    private static final int SFTP_PORT = 2022;
    public static final String USERNAME = "user";
    private static final String PASSWORD = "secret";

    public WebdavContainer() {
        super("drakkan/sftpgo:v2.5.5");
        this
            .withCommand(
                "sftpgo",
                "portable",
                "--directory",
                "/tmp",
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
            .withExposedPorts(WEBDAV_PORT, SFTP_PORT)
            .waitingFor(Wait.forHttp("/").forPort(WEBDAV_PORT).withBasicCredentials(USERNAME, PASSWORD).forStatusCode(207));
        ;
    }

    public int getWebdavPort() {
        return getMappedPort(WEBDAV_PORT);
    }

    public String getWebdavUrl() {
        return "webdav://" + USERNAME + ':' + PASSWORD + '@' + getHost() + ":" + getWebdavPort();
    }

}
