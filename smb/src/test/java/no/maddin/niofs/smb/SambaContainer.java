package no.maddin.niofs.smb;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class SambaContainer extends GenericContainer<SambaContainer> {
    public SambaContainer(String hostPath) {
        super("dperson/samba");
        this.waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.of(3, ChronoUnit.MINUTES)))
            .withEnv("WORKGROUP", "TEST")
            .withCommand("-s", "public;/mount")
            .withFileSystemBind(hostPath, "/mount")
            .withExposedPorts(139, 445);
    }

    public String getGuestIpAddress() {
        return getContainerInfo().getNetworkSettings().getIpAddress();
    }
}
