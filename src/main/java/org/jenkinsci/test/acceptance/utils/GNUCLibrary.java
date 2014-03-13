package org.jenkinsci.test.acceptance.utils;

/**
 * @author Kohsuke Kawaguchi
 */
public interface GNUCLibrary {
    int kill(int pid, int signal);
}
