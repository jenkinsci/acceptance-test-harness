package org.jenkinsci.test.acceptance.log;

import hudson.remoting.Asynchronous;

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
    @Asynchronous
    void processLine(String line) throws IOException;

    /**
     * Indicates the EOF.
     *
     * @param t
     *      if the termination of log source is unexpected, indicate the cause of the problem.
     */
    @Asynchronous
    void processClose(Exception t);
}
