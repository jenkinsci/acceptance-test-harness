package org.jenkinsci.test.acceptance.docker.fixtures;

import java.io.IOException;
import java.net.URL;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Runs gitserver container.
 */
@DockerFixture(id = "git", ports = 22)
public class GitContainer extends DockerContainer {

    private static final String REPO_DIR = "/home/git/git-plugin.git";

    public URL getUrl() throws IOException {
        return new URL("http://localhost:" + port(22));
    }

    public String getRepoUrl() {
        return "ssh://git@localhost:" + port(22) + REPO_DIR;
    }

}
