package org.jenkinsci.test.acceptance.log;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.concurrent.BasicFuture;

/**
 * Receives log as {@link LogListener}, and  provides a regular expression pattern matching.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class LogWatcher implements LogListener {
    /**
     * All the listeners in here is {@link Watcher}s.
     */
    protected final LogSplitter splitter = new LogSplitter();

    @Override
    public void processLine(String line) throws IOException {
        splitter.processLine(line);
    }

    @Override
    public void processClose(Exception t) {
        splitter.processClose(t);
    }

    /**
     * Starts watching an expression in the output.
     * <p>
     * Returned future will signal when the expression is found.
     */
    public Future<Matcher> watch(Pattern regexp) {
        Watcher w = new Watcher(regexp);
        splitter.addLogListener(w);
        return w;
    }

    class Watcher extends BasicFuture<Matcher> implements LogListener {
        private final Pattern pattern;

        public Watcher(Pattern pattern) {
            super(null);
            this.pattern = pattern;
        }

        @Override
        public void processLine(String line) throws IOException {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                completed(m);
                splitter.removeLogListener(this);
            }
        }

        @Override
        public void processClose(Exception t) {
            if (t==null)
                t = new IOException("Regular termination");
            failed(t);
        }
    }
}
