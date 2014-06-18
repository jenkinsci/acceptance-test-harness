package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URL;

/**
 * Runs gitserver container.
 */
@DockerFixture(id = "git", ports = 22)
public class GitContainer extends DockerContainer {

    private static final String REPO_DIR = "/home/git/gitRepo";
    public static final String REPO_NAME = "gitRepo";

    public int port() {
        return port(22);
    }

    public URL getUrl() throws IOException {
        return new URL("http://localhost:" + port(22));
    }

    public String getRepoUrl() {
        return "ssh://git@localhost:" + port(22) + REPO_DIR;
    }
}
