package org.jenkinsci.test.acceptance.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.jenkinsci.test.acceptance.log.LogListenable;
import org.jenkinsci.test.acceptance.log.LogListener;
import org.jenkinsci.test.acceptance.log.LogPrinter;
import org.jenkinsci.test.acceptance.log.LogReader;
import org.jenkinsci.test.acceptance.log.LogSplitter;
import org.jenkinsci.test.acceptance.log.LogWatcher;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.*;

/**
 * {@link LogWatcher} for monitoring output from Jenkins
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsLogWatcher implements LogListenable, Closeable {
    /**
     * Signals when Jenkins is ready for action.
     */
    public final Future<Matcher> ready;

    /**
     * Signals when there's a port conflict, which is a common error.
     */
    public final Future<?> portConflict;

    public final File logFile;

    private InputStream pipe;

    /**
     * Thread that reads log output from Jenkins.
     */
    protected final Thread reader;

    /**
     * Splits the log file to multiple sinks.
     */
    private final LogSplitter splitter = new LogSplitter();

    private final LogWatcher watcher = new LogWatcher();

    /**
     * @param id
     *      Short ID that indicates the log that we are watching.
     */
    public JenkinsLogWatcher(String id, InputStream pipe, File logFile) throws FileNotFoundException {
        this.logFile = logFile;
        this.pipe = new TeeInputStream(pipe,new FileOutputStream(logFile));

        splitter.addLogListener(new LogPrinter(id));
        splitter.addLogListener(watcher);
        reader = new Thread(new LogReader(pipe,splitter),"Log reader: "+id);

        ready = watcher.watch(Pattern.compile(" Completed initialization"));
        portConflict = watcher.watch(Pattern.compile("java.net.BindException: Address already in use"));
    }

    /**
     * Starts scanning logs.
     */
    public void start() {
        reader.start();
    }

    @Override
    public void close() throws IOException {
        if(pipe != null){
            pipe.close();
            pipe = null;
        }
    }

    /**
     * Block until Jenkins is up and running
     */
    public void waitTillReady() {
        try {
            ready.get(TIMEOUT, SECONDS);
        } catch (TimeoutException e) {
            if (portConflict.isDone())
                throw new RuntimeException("Port conflict detected");

            throw new RuntimeException(failedToLoadMessage());
        } catch (InterruptedException | ExecutionException e) {
            throw new Error(failedToLoadMessage(), e);
        }
    }

    private String failedToLoadMessage() {
        String msg = getClass()+": Could not bring up a Jenkins server";
        msg += "\nprocess is " + (reader.isAlive() ? "alive" : "dead");
        msg += "\nnow = " + new Date();
        try {
            msg += "\n" + FileUtils.readFileToString(logFile);
        } catch (IOException _) {
            // ignore
        }
        return msg;
    }

    @Override
    public void addLogListener(LogListener l) {
        splitter.addLogListener(l);
    }

    @Override
    public void removeLogListener(LogListener l) {
        splitter.removeLogListener(l);
    }

    public void waitForLogged(Pattern regexp, long seconds) {
        try {
            watcher.watch(regexp).get(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            throw new AssertionError(ex);
        }
    }

    public static final int DEFAULT_TIMEOUT = 300; // seconds

    public static final int TIMEOUT = System.getenv("STARTUP_TIME") != null && Integer.parseInt(System.getenv("STARTUP_TIME")) > 0
            ? Integer.parseInt(System.getenv("STARTUP_TIME")) : DEFAULT_TIMEOUT;
}
