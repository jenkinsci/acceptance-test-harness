package org.jenkinsci.test.acceptance.utils;

import org.apache.http.concurrent.BasicFuture;
import org.jenkinsci.test.acceptance.controller.LogListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Future;

/**
 * Reads {@link InputStream} and delivers logs to {@link LogListener}.
 *
 * @author Kohsuke Kawaguchi
 */
public class LogReader implements Runnable {
    /**
     * Stream to read from.
     */
    private final InputStream source;

    /**
     * Signals when the log processing terminates.
     */
    private final BasicFuture<Void> done;

    private final LogListener listener;

    public LogReader(InputStream source, LogListener listener) {
        this.source = source;
        this.listener = listener;
        this.done = new BasicFuture<>(null);
    }

    /**
     * Returns a {@link Future} that signals when the log processing is completed.
     */
    public Future<?> getDone() {
        return done;
    }

    @Override
    public void run() {
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(source));
            while ((line = reader.readLine()) != null) {
                listener.processLine(line);
            }
            System.out.println("Jenkins is stopped");
        } catch (Exception e) {
            listener.processClose(new Exception("Process has terminated", e));
            done.failed(e);
        } finally {
            listener.processClose(null);
            done.completed(null);
        }
    }
}
