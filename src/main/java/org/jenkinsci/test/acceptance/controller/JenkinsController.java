package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jenkinsci.test.acceptance.ControllerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author: Vivek Pandey
 */
@ExtensionPoint
public abstract class JenkinsController {
    protected static final String WORKSPACE = System.getenv("WORKSPACE") != null? System.getenv("WORKSPACE") : System.getProperty("user.dir");
    protected static final String JENKINS_DEBUG_LOG = WORKSPACE + "/last_test.log";

    private boolean isRunning;

    protected FileWriter logger;
    protected LogWatcher logWatcher;

    protected JenkinsController(Map<String,String> opts) {
        if(FileUtils.fileExists(JENKINS_DEBUG_LOG)){
            FileUtils.removePath(JENKINS_DEBUG_LOG);
        }
        try {
            File f = new File(JENKINS_DEBUG_LOG);
            f.createNewFile();
            this.logger = new FileWriter(f);
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

    public abstract String getTempDir();

    public String getSlaveJarPath(){
        return getTempDir()+"/war/WEB-INF/slave.jar";
    }

    protected String downloadPathElement(){
        String source = "http://updates.jenkins-ci.org/latest/form-element-path.hpi";
        String target =  WORKSPACE+"/path-element.hpi";
        if(!FileUtils.fileExists(target)){
            FileOutputStream fileWriter = null;
            HttpURLConnection uc = null;
            try {
                fileWriter = new FileOutputStream(target);
                HttpClient client = new HttpClient();
                GetMethod get = new GetMethod(source);
                get.setFollowRedirects(true);
                int status = client.executeMethod(get);
                if(status != 200){
                    throw new RuntimeException("Failed to get form-element-path.hpi: "+get.getResponseBodyAsString());
                }
                IOUtil.copy(get.getResponseBodyAsStream(), fileWriter);
            } catch (IOException e) {
                throw new ControllerException(String.format("Failed to open %s for write operation", target), e);
            }finally {
                if(fileWriter != null){
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e) {
                        throw new ControllerException(String.format("Failed to close %s after writing",target),e);
                    }
                }
            }
        }
        return target;
    }


    public void waitForUpdates() {
        // TODO
    }
}
