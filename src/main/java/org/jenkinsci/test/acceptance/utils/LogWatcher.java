package org.jenkinsci.test.acceptance.utils;

import org.apache.http.concurrent.BasicFuture;
import org.jenkinsci.test.acceptance.controller.LogListener;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads {@link InputStream} and provides a regular expression pattern matching.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class LogWatcher implements Closeable {
    private InputStream pipe;

    /**
     * All the listeners in here is {@link Watcher}s.
     */
    private final LogSplitter splitter = new LogSplitter();

    /**
     * Thread that reads input stream.
     */
    protected final Thread reader;

    /**
     * Signals when the process terminates.
     */
    public final Future<?> done;

    /**
     * @param pipe
     *      Output from the process
     */
    public LogWatcher(InputStream pipe) {
        this.pipe = pipe;
        final BasicFuture<Void> done = new BasicFuture<>(null);
        this.done = done;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                String line;
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(LogWatcher.this.pipe));
                    while ((line = reader.readLine()) != null) {
                        splitter.processLine(line);
                    }
                    System.out.println("Jenkins is stopped");
                } catch (Exception e) {
                    failWatchers(new Exception("Process has terminated",e));
                    done.failed(e);
                } finally {
                    failWatchers(new Exception("Process has terminated"));
                    done.completed(null);
                }
            }

            private void failWatchers(Exception e) {
                for (LogListener w : splitter.getListeners()) {
                    ((Watcher)w).failed(e);
                }
                splitter.clear();
            }
        };
        reader = new Thread(r);
    }

    /**
     * Starts scanning logs.
     */
    public void start() {
        reader.start();
    }

    /**
     * Starts watching an expression in the output.
     *
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
    }

    public void close() throws IOException {
        if(pipe != null){
            pipe.close();
            pipe = null;
        }
    }
}
