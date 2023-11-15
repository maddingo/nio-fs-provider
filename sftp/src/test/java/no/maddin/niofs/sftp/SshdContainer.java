package no.maddin.niofs.sftp;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

public class SshdContainer extends GenericContainer<SshdContainer> {
    public SshdContainer() {
        super("docker.io/panubo/sshd:1.5.0");
        this.withExposedPorts(2222)
            .withEnv("SSH_ENABLE_PASSWORD_AUTH", "true")
            .withEnv("SSH_USERS", "testuser:1000:1000")
            .withClasspathResourceMapping("/entrypoint.d/", "/etc/entrypoint.d/", BindMode.READ_ONLY);
    }
}
