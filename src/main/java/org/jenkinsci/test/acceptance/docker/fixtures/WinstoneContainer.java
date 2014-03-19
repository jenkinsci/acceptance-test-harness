package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Fixture for running Winstone in it.
 *
 * @author Kohsuke Kawaguchi
 */
@DockerFixture(id="winstone",ports={22,8080})
public class WinstoneContainer extends SshdContainer {
}
