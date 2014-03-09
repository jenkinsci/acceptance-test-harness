package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;
import java.net.URL;

/**
 * @author: Vivek Pandey
 */
@ExtensionPoint
public abstract class JenkinsController implements Closeable {
    /**
     * directory on the computer where this code is running that points to a directory
     * where test code can place log files, cache files, etc.
     * Note that this directory might not exist on the Jenkins master, since it can be
     * running on a separate computer.
     */
    protected static final String WORKSPACE = System.getenv("WORKSPACE") != null? System.getenv("WORKSPACE") : System.getProperty("user.dir");

    protected static final String JENKINS_DEBUG_LOG = WORKSPACE + "/last_test.log";

    private boolean isRunning;

    protected final  OutputStream logger;

    protected JenkinsController() {
        if(FileUtils.fileExists(JENKINS_DEBUG_LOG)){
            FileUtils.removePath(JENKINS_DEBUG_LOG);
        }
        try {
            File f = new File(JENKINS_DEBUG_LOG);
            f.createNewFile();
            this.logger = new FileOutputStream(f);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log file "+ JENKINS_DEBUG_LOG);
        }
    }

    /**
     * Starts Jenkins
     *
     * @throws IOException
     */
    public void start() throws IOException{
        if(!isRunning){
            startNow();
            this.isRunning = true;
        }
    }

    /**
     * Jenkins controller specific startup logic
     */
    public abstract void startNow() throws IOException;


    /**
     * Stops Jenkins
     *
     * @throws IOException
     */
    public void stop() throws IOException{
        if(isRunning){
            stopNow();
        }
    }

    /**
     * Alias for {@link #stop()}
     */
    public final void close() throws IOException {
        stop();
    }

    /**
     * Jenkins controller specific stop logic
     */
    public abstract void stopNow() throws IOException;


    /**
     * Stops and starts running Jenkins
     * @throws IOException
     */
    public void restart() throws IOException{
        stop();
        start();
    }

    public boolean isRunning(){
        return isRunning;
    }

    /**
     * Gives URL where Jenkins is listening. Must end with "/"
     */
    public abstract URL getUrl();

    public abstract void diagnose();

    public abstract void tearDown();

    /**
     * Downloads the latest version of the form-element-path plugin that we use for testing.
     */
    public static File downloadPathElement() {
        String source = "http://updates.jenkins-ci.org/latest/form-element-path.hpi";
        File target =  new File(WORKSPACE,"path-element.hpi");
        if (!target.exists()) {
            try(FileOutputStream fos = new FileOutputStream(target)) {
                HttpClient client = new HttpClient();
                GetMethod get = new GetMethod(source);
                get.setFollowRedirects(true);
                int status = client.executeMethod(get);
                if (status != 200) {
                    throw new RuntimeException("Failed to get form-element-path.hpi: " + get.getResponseBodyAsString());
                }
                IOUtil.copy(get.getResponseBodyAsStream(), fos);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to open %s for write operation", target), e);
            }
        }
        return target;
    }


    public void waitForUpdates() {
        // TODO
    }
}
