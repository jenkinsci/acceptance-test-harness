package org.jenkinsci.test.acceptance.log;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Prints out the received log with a prefix.
 *
 * @author Kohsuke Kawaguchi
 */
public class LogPrinter implements LogListener {
    private final String prefix;

    public LogPrinter(String id) {
        this.prefix = id==null ? "" : id+"|";
    }

    @Override
    public void processLine(String line) throws IOException {
        LOGGER.info(prefix + line);
    }

    @Override
    public void processClose(Exception t) {
    }

    private static final Logger LOGGER = Logger.getLogger(LogPrinter.class.getName());
}
