package no.maddin.niofs.smb;

import com.github.dockerjava.api.model.ContainerNetwork;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.Socket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public class SambaContainer extends GenericContainer<SambaContainer> {
    public SambaContainer(String hostPath) {
        super("dperson/samba");
        this.waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.of(3, ChronoUnit.MINUTES)))
            .withEnv("WORKGROUP", "TEST")
            .withCommand("-s", "public;/mount")
            .withFileSystemBind(hostPath, "/mount")
            .withExposedPorts(139, 445);
    }

    /**
     * For testing only.
     */
    private SambaContainer() {
        super("alpine:latest");
    }

    public static SambaContainer runningOr(Supplier<? extends SambaContainer> supplier) {
        if (System.getenv("SAMBA_HOST") != null) {
            return new SambaContainer() {
                @Override
                public String getGuestIpAddress() {
                    return System.getenv("SAMBA_HOST");
                }

                @Override
                public void start() {
                    // don't do anything, rely on the SAMBA_HOST being started
                }
            };
        } else{
            return supplier.get();
        }
    }

    @Override
    public void start() {
        super.start();
        try {
            Thread.sleep(30_000L); // seems like the container is not ready, even though the health check is ok
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public String getGuestIpAddress() {
        return getContainerInfo()
            .getNetworkSettings()
            .getNetworks()
            .values()
            .stream()
            .findFirst()
            .map(ContainerNetwork::getIpAddress)
            .orElseThrow(() -> new RuntimeException("no network found"));
    }
}
