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

    public String host() {
        return ipBound(22);
    }
    
    public int port() {
        return port(22);
    }

    public URL getUrl() throws IOException {
        return new URL("http://" + host() + ":" + port());
    }

    /** URL visible from the host. */
    public String getRepoUrl() {
        return "ssh://git@" + host() + ":" + port() + REPO_DIR;
    }

    @Deprecated
    public String getRepoUrlInsideDocker() throws IOException {
        return "ssh://git@" + getIpAddress() + REPO_DIR;
    }

    /**
     * URL visible from other Docker containers.
     * @param alias an alias for this container’s {@link #getCid} passed to {@code --link}
     */
    public String getRepoUrlInsideDocker(String alias) throws IOException {
        return "ssh://git@" + alias + REPO_DIR;
    }

}
