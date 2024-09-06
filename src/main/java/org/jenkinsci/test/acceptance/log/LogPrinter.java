package org.jenkinsci.test.acceptance.log;

import java.io.IOException;

/**
 * Prints out the received log with a prefix.
 *
 * @author Kohsuke Kawaguchi
 */
public class LogPrinter implements LogListener {
    private final String prefix;

    public LogPrinter(String id) {
        this.prefix = id == null ? "" : id + "|";
    }

    @Override
    public void processLine(String line) throws IOException {
        System.out.println(prefix + line);
    }

    @Override
    public void processClose(Exception t) {}
}
