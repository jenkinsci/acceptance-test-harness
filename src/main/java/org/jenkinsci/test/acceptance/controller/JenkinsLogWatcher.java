package org.jenkinsci.test.acceptance.controller;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.jenkinsci.test.acceptance.log.LogListenable;
import org.jenkinsci.test.acceptance.log.LogListener;
import org.jenkinsci.test.acceptance.log.LogReader;
import org.jenkinsci.test.acceptance.log.LogSplitter;
import org.jenkinsci.test.acceptance.log.LogWatcher;

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
     * @param printer
     *      The printer to use to write the Jenkins logging statements to
     */
    public JenkinsLogWatcher(String id, InputStream pipe, File logFile, final LogListener printer) throws FileNotFoundException {
        this.logFile = logFile;
        this.pipe = new TeeInputStream(pipe,new FileOutputStream(logFile));

        splitter.addLogListener(printer);
        splitter.addLogListener(watcher);
        reader = new Thread(new LogReader(this.pipe,splitter),"Log reader: "+id);

        ready = watcher.watch(Pattern.compile("Jenkins is fully up and running"));
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
            ready.get(JenkinsController.STARTUP_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            if (portConflict.isDone())
                throw new RuntimeException("Port conflict detected");

            throw new RuntimeException(failedToLoadMessage());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(failedToLoadMessage(), e);
        }
    }

    private String failedToLoadMessage() {
        String msg = getClass()+": Could not bring up a Jenkins server";
        msg += "\nprocess is " + (reader.isAlive() ? "alive" : "dead");
        msg += "\nnow = " + new Date();
        try {
            msg += "\n" + FileUtils.readFileToString(logFile, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
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
}
