package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URL;

/**
 * Runs Foreman container.
 *
 */
//CS IGNORE MagicNumber FOR NEXT 2 LINES. REASON: Mock object.
@DockerFixture(id = "foreman" , ports = 32768)
public class ForemanContainer extends DockerContainer {

    /**
     * URL of Foreman.
     * @return URL.
     * @throws IOException if occurs.
     */
    //CS IGNORE MagicNumber FOR NEXT 2 LINES. REASON: Mock object.
    public URL getUrl() throws IOException {
        return new URL("http://" + getIpAddress() + ":3000");
    }
}
