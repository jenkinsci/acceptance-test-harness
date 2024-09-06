package org.jenkinsci.test.acceptance.log;

import org.jenkinsci.test.acceptance.controller.JenkinsController;

/**
 * Produces line-by-line log.
 * <p>
 * Among other things, optionally implemented by {@link JenkinsController} that provides access
 * to the console output.
 *
 * @author Kohsuke Kawaguchi
 */
public interface LogListenable {
    void addLogListener(LogListener l);

    void removeLogListener(LogListener l);
}
