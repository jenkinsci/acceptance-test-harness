package org.jenkinsci.test.acceptance.controller;

import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: Vivek Pandey
 */
public class LogWatcher {
    private final boolean silent;
    private final String pattern;
    private final AtomicReference<String> logPattern = new AtomicReference<>(null);
    private final AtomicReference<InputStream> jenkinsPipe = new AtomicReference<>();
    private final OutputStream log;
    private final AtomicBoolean ready = new AtomicBoolean(false);

    private final List<String> loggedLines = new ArrayList<>();
    private final AtomicBoolean logFound = new AtomicBoolean(false);

    private static final int DEFAULT_TIMEOUT = 100;//100 sec
    private static final long DEFAULT_SLEEP_TIME = 500;//0.5 sec
    private static final int TIMEOUT = System.getenv("STARTUP_TIME") != null && Integer.getInteger(System.getenv("STARTUP_TIME")) > 0
            ? Integer.getInteger(System.getenv("STARTUP_TIME")) : DEFAULT_TIMEOUT;

    public LogWatcher(final InputStream pipe, final OutputStream log, Map<String,String> opts) {
        this.silent = opts.get("silent") != null && opts.get("silent").equalsIgnoreCase("true");
        if(opts.get("pattern") == null){
            this.pattern = " Completed initialization";
        }else{
            this.pattern = opts.get("pattern");
        }
        if(opts.get("log_pattern") != null){
            this.logPattern.set(opts.get("log_pattern"));
        }

        this.jenkinsPipe.set(pipe);
        this.log = log;



        Runnable r = new Runnable(){

            @Override
            public void run() {
                String line = null;
                try {
                    int lineCount=0;

                    BufferedReader reader = new BufferedReader(new InputStreamReader(jenkinsPipe.get()));
                    while((line = reader.readLine()) != null){
                        logLine(line+"\n");
                        if(ready.get()){
                            continue;
                        }

                        if(line.contains(pattern)){
                            ready.set(true);
                        }else{
                            if(!silent){
                                if(lineCount%5 == 0){
                                    System.out.print(".");
                                }
                                lineCount++;
                            }
                        }
                    }
                    ready.set(false);
                } catch (IOException e) {
                    System.out.println("Jenkins is stopped");
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    /**
     * Block until Jenkins is up and running
     * @param expected true then checks if Jenkins is ready, otherwise checks if its shutdown.
     *                 If null default value is true
     */
    public void waitTillReady(Boolean expected) throws InterruptedException {
        expected = (expected == null)?true:expected;
        long startTime = System.currentTimeMillis();
        while(ready.get() != expected && (System.currentTimeMillis()-startTime)/1000 < TIMEOUT){
            Thread.sleep(500); //sleep for 0.5 sec
        }

        if(hasLogged("java.net.BindException: Address already in use")){
            throw new RuntimeException("Port conflict detected");
        }

        if(ready.get() != expected){
            String msg = expected ? "Could not bring up a Jenkins server" : "Shut down of Jenkins server had timed out";
            throw new RuntimeException(msg);
        }
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

    public boolean hasLogged(String regex){
        for(String line:loggedLines){
            if(line.matches(regex)){
                return true;
            }
        }
        return false;
    }

    public void close() throws IOException {
        if(jenkinsPipe != null){
            jenkinsPipe.get().close();
            jenkinsPipe.set(null);
        }
    }

    public String fullLog(){
        StringBuilder sb = new StringBuilder();
        for(String line: loggedLines){
            sb.append(line);
        }
        return sb.toString();
    }


    private void logLine(String line) throws IOException {
        log.write(line.getBytes("UTF-8"));
        log.flush();
        loggedLines.add(line);

        if(logPattern.get() != null){
            if(line.contains(logPattern.get())){
                logFound.set(true);
            }
        }
    }


    public class JenkinsPipe{

        private final ProcessInputStream pis;
        public JenkinsPipe(ProcessInputStream pipe){
            this.pis = pipe;
        }


        public String readLine() throws IOException {
            int c = pis.read();
            if(c < 0){
                return null;
            }
            StringBuilder line = new StringBuilder();
            while(c != -1){
                line.append(c);
                if(c==10){
                    break;
                }
                c = pis.read();
            }
            return line.toString();
        }

    }
}
