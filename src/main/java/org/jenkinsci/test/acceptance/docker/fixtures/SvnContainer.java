package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URL;

/**
 * Runs stock svn container.
 *
 * @author Matthias Karl
 */
@DockerFixture(id="svn",ports=80)
public class SvnContainer extends DockerContainer {
    private static final String REPO_NAME = "/svn";
    /**
     * URL of SNV
     */
    public URL getUrl() throws IOException {
        return new URL("http://localhost:"+port(80)+REPO_NAME);
    }
}
