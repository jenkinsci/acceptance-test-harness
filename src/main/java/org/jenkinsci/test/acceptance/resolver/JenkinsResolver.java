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

    /**
     * Makes jenkins.war available at the the default JENKINS_WAR_TARGET_LOCATION
     *
     * Returns the installed Jenkins war path
     */
    String materialize(Machine machine);

    public static final String JENKINS_WAR_TARGET_LOCATION = "./.jenkins_test/jenkins.war";
}
