package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.ExtensionPoint;

/**
 * Extension point for instantiating {@link JenkinsController} from command-line options / environments.
 *
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public interface JenkinsControllerFactory {
    /**
     * Unique short name that distinguishes this controller from others.
     * <p>
     * User can select the factory by specifying its ID to the "TYPE" environment variable.
     */
    String getId();

    JenkinsController create();
}
