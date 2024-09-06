package org.jenkinsci.test.acceptance.log;

import java.io.IOException;

/**
 * Does not print anything.
 *
 * @author Ullrich Hafner
 */
public class NullPrinter implements LogListener {
    @Override
    public void processLine(final String line) throws IOException {
        // nothing to print
    }

    @Override
    public void processClose(final Exception t) {
        // nothing to print
    }
}
