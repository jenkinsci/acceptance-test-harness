package org.jenkinsci.test.acceptance.controller;

import java.io.IOException;

/**
 * Receives line-by-line logs from {@link LogListenable}.
 *
 * @see LogListenable
 * @author Kohsuke Kawaguchi
 */
public interface LogListener {
    /**
     * Receives log output from Jenkins process one line at a time, in the order.
     */
    void processLine(String line) throws IOException;
}
