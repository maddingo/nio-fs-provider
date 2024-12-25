package no.maddin.niofs.testutil;

import org.testcontainers.lifecycle.Startable;

import java.net.URI;

public interface BasicTestContainer extends Startable {
    URI getBaseUri(String protocol);
}
