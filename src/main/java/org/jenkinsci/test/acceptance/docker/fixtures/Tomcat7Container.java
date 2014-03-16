package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URL;

/**
 * Runs stock Tomcat 7 container.
 *
 * @author Kohsuke Kawaguchi
 */
@DockerFixture(id="tomcat7",ports=8080)
public class Tomcat7Container extends DockerContainer {
    /**
     * URL of Tomcat.
     */
    public URL getUrl() throws IOException {
        return new URL("http://localhost:"+port(8080));
    }
}
