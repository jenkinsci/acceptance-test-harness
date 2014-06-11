package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URL;

/**
 * Runs  XMPP / Jabber server container.
 */
@DockerFixture(id = "xmpp", ports = 5222)
public class XMPPContainer extends DockerContainer {

//    private static final String REPO_DIR = "/home/git/gitRepo.git";
//
//    public URL getUrl() throws IOException {
//        return new URL("http://localhost:" + port(22));
//    }
//
//    public String getRepoUrl() {
//        return "ssh://git@localhost:" + port(22) + REPO_DIR;
//    }

}