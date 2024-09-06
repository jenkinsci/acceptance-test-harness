package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerFixture;

@DockerFixture(id = "javagit", ports = 22)
public class JavaGitContainer extends GitContainer {
    /** Local path. */
    @Override
    public String getRepoUrl() {
        return "file:///" + REPO_DIR;
    }
}
