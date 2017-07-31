package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * https://hub.docker.com/r/kristophjunge/test-saml-idp/
 */
@DockerFixture(id = "saml", ports = {80, 443})
public class SAMLContainer extends DockerContainer {

    public String host() {
        return ipBound(80);
    }

    public int port() {
        return port(80);
    }
}
