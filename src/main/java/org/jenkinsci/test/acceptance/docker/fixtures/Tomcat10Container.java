package org.jenkinsci.test.acceptance.docker.fixtures;

import java.io.IOException;
import java.net.URL;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Runs stock Tomcat 7 container.
 *
 * @author Kohsuke Kawaguchi
 */
@DockerFixture(id="tomcat10",ports=8080)
public class Tomcat10Container extends DockerContainer {
    /**
     * URL of Tomcat.
     */
    public URL getUrl() throws IOException {
        return new URL("http://"+ipBound(8080)+":"+port(8080));
    }
}
