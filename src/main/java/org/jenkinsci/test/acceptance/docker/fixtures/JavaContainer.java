package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Fixture capable of running java programs over ssh.
 *
 * @author Kohsuke Kawaguchi
 */
@DockerFixture(id="java",ports={22,8080})
public class JavaContainer extends SshdContainer {
}
