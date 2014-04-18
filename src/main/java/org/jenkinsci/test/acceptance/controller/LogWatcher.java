package org.jenkinsci.test.acceptance.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Monitor console output from Jenkins and detect certain state changes.
 *
 * @author: Vivek Pandey
 */
public class LogWatcher {
    private final String pattern;
    private final AtomicReference<String> logPattern = new AtomicReference<>(null);
    private InputStream jenkinsPipe;

    private final AtomicBoolean ready = new AtomicBoolean(false);

    private final List<String> loggedLines = new ArrayList<>();
    private final AtomicBoolean logFound = new AtomicBoolean(false);

    /**
     * Thread that reads input stream.
     */
    private final Thread reader;

    private static final int DEFAULT_TIMEOUT = 300;//100 sec
    private static final long DEFAULT_SLEEP_TIME = 500;//0.5 sec
    private static final int TIMEOUT = System.getenv("STARTUP_TIME") != null && Integer.parseInt(System.getenv("STARTUP_TIME")) > 0
            ? Integer.parseInt(System.getenv("STARTUP_TIME")) : DEFAULT_TIMEOUT;


    /**
     * @param pipe
     *      Output from Jenkins is expected to come here.
     */
    public LogWatcher(final InputStream pipe, Map<String,String> opts) {
        if(opts.get("pattern") == null){
            this.pattern = " Completed initialization";
        }else{
            this.pattern = opts.get("pattern");
        }
        if(opts.get("log_pattern") != null){
            this.logPattern.set(opts.get("log_pattern"));
        }

        this.jenkinsPipe = pipe;


        Runnable r = new Runnable() {

            @Override
            public void run() {
                String line;
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jenkinsPipe));
                    while ((line = reader.readLine()) != null) {
                        logLine(line + "\n");
                        if (ready.get()) {
                            continue;
                        }

                        if (line.contains(pattern)) {
                            ready.set(true);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Jenkins is stopped");
                } finally {
                    ready.set(false);
                }
            }
        };
        reader = new Thread(r);
        reader.start();
    }

    /**
     * Block until Jenkins is up and running
     * @param expected true then checks if Jenkins is ready, otherwise checks if its shutdown.
     *                 If null default value is true
     */
    public void waitTillReady(Boolean expected) throws InterruptedException {
        expected = (expected == null)?true:expected;
        long startTime = System.currentTimeMillis();

        while(reader.isAlive() && (System.currentTimeMillis()-startTime)/1000 < TIMEOUT){
            if (ready.get()==expected)
                return; // condition met

            Thread.sleep(500); //sleep for 0.5 sec
        }

        if(hasLogged("java.net.BindException: Address already in use")){
            throw new RuntimeException("Port conflict detected");
        }

        String msg = getClass()+": "+ (expected ? "Could not bring up a Jenkins server" : "Shut down of Jenkins server had timed out");
        msg += "\nprocess is " + (reader.isAlive() ? "alive" : "dead");
        msg += "\nnow = " + new Date();
        msg += "\n" + fullLog();
        throw new RuntimeException(msg);
    }

    /**
     * Wait until given regex is matched in the log.
     * @param regex  Regex to match in the log
     * @param timeout if no match found within this timeout then return
     */
    public void waitUntilLogged(String regex, Integer timeout) throws InterruptedException, TimeoutException {
        timeout = (timeout == null) ? 60 : timeout;
        logPattern.set(regex);

        if(hasLogged(regex)){
            return;
        }

        long start = System.currentTimeMillis();

        while((System.currentTimeMillis() - start)/1000 < timeout){
            if(logFound.get()){
                logPattern.set(null);
                logFound.set(false);
                return;
            }
            Thread.sleep(1000);
        }
        throw new TimeoutException(String.format("Pattern '%s' was not logged within %s seconds", regex,timeout));

    }

    public class TimeoutException extends Exception{

        public TimeoutException(String format) {
            super(format);
        }
    }

    public boolean hasLogged(String regex) {
        synchronized (loggedLines) {
            for (String line : loggedLines) {
                if (line.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void close() throws IOException {
        if(jenkinsPipe != null){
            jenkinsPipe.close();
            jenkinsPipe = null;
        }
    }

    public String fullLog() {
        synchronized (loggedLines) {
            StringBuilder sb = new StringBuilder();
            for (String line : loggedLines) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private void logLine(String line) throws IOException {
        synchronized (loggedLines) {
            loggedLines.add(line);
        }

        if(logPattern.get() != null){
            if(line.contains(logPattern.get())){
                logFound.set(true);
            }
        }
    }
}
