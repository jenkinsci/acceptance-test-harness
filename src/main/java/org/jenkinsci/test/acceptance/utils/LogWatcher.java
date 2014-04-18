package org.jenkinsci.test.acceptance.utils;

import org.apache.http.concurrent.BasicFuture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Monitor standard output from a process and flags if a specific string appears.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class LogWatcher {
    private InputStream pipe;

    /**
     * Expressions we are watching.
     */
    private final List<Watcher> watchers = new ArrayList<>();

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
                        synchronized (watchers) {
                            Iterator<Watcher> itr = watchers.iterator();
                            while (itr.hasNext()) {
                                Watcher w =  itr.next();
                                if (w.feed(line))
                                    itr.remove();
                            }
                        }
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
                synchronized (watchers) {
                    for (Watcher w : watchers) {
                        w.failed(e);
                    }
                    watchers.clear();
                }
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
        synchronized (watchers) {
            Watcher w = new Watcher(regexp);
            watchers.add(w);
            return w;
        }
    }

    class Watcher extends BasicFuture<Matcher> {
        private final Pattern pattern;

        public Watcher(Pattern pattern) {
            super(null);
            this.pattern = pattern;
        }

        boolean feed(String line) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                completed(m);
                return true;
            } else {
                return false;
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
