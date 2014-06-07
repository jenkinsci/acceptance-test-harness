package org.jenkinsci.test.acceptance.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.jenkinsci.test.acceptance.utils.LogWatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.*;

/**
 * {@link LogWatcher} for monitoring output from Jenkins
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsLogWatcher extends LogWatcher {
    /**
     * Signals when Jenkins is ready for action.
     */
    public final Future<Matcher> ready;

    /**
     * Signals when there's a port conflict, which is a common error.
     */
    public final Future<?> portConflict;

    public final File logFile;

    private final String prefix;

    /**
     * @param id
     *      Short ID that indicates the log that we are watching.
     */
    public JenkinsLogWatcher(String id, InputStream pipe, File logFile) throws FileNotFoundException {
        super(new TeeInputStream(pipe,new FileOutputStream(logFile)));

        this.logFile = logFile;
        this.prefix = id==null ? null : id+"|";

        ready = watch(Pattern.compile(" Completed initialization"));
        portConflict = watch(Pattern.compile("java.net.BindException: Address already in use"));
    }

    @Override
    public void processLine(String line) {
        if (prefix!=null)
            System.out.print(prefix);
        System.out.println(line);
        super.processLine(line);
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

    public static final int DEFAULT_TIMEOUT = 300;//100 sec

    public static final int TIMEOUT = System.getenv("STARTUP_TIME") != null && Integer.parseInt(System.getenv("STARTUP_TIME")) > 0
            ? Integer.parseInt(System.getenv("STARTUP_TIME")) : DEFAULT_TIMEOUT;
}
