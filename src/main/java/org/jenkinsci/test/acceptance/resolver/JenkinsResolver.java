package org.jenkinsci.test.acceptance.resolver;

import org.jenkinsci.test.acceptance.controller.Machine;

/**
 * Make 'jenkins.war' (subject under test) available on {@link Machine}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface JenkinsResolver {
    /**
     * Makes jenkins.war available at the specified path on the specified machine.
     *
     * How it does so is up to implementations.
     */
    void materialize(Machine machine, String path);
}
