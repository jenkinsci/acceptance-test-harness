package org.jenkinsci.test.acceptance.utils;

import org.jenkinsci.test.acceptance.controller.LogListener;

import java.io.IOException;

/**
 * Prints out the received log with a prefix.
 *
 * @author Kohsuke Kawaguchi
 */
public class LogPrinter implements LogListener {

    private final String prefix;

    public LogPrinter(String id) {
        this.prefix = id==null ? null : id+"|";
    }

    @Override
    public void processLine(String line) throws IOException {
        if (prefix!=null)
            System.out.print(prefix);
        System.out.println(line);
    }

    @Override
    public void processClose(Exception t) {
    }
}
