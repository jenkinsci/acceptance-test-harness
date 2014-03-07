package org.jenkinsci.test.acceptance.controller;

/**
 * Represents a system accessible through SSH to run commands (like Jenkins masters and slaves.)
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public interface Machine {
    /**
     * Connects to this computer over SSH so that we can do stuff
     *
     * TODO: maybe consider using Overthere
     */
    Ssh connect();

    String getPublicIpAddress();

    String getUser();

    void terminate();

    /**
     * Allocates a TCP/IP port on the machine to be used by a test
     * (for example to let Jenkins listen on this port for HTTP.)
     */
    int getNextAvailablePort();
}
