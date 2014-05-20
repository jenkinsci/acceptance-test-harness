package org.jenkinsci.test.acceptance.controller;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import com.google.inject.Injector;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jenkinsci.test.acceptance.guice.AutoCleaned;

import com.cloudbees.sdk.extensibility.ExtensionPoint;

/**
 * Starts/stops Jenkins and exposes where it is running.
 *
 * <p>
 * This abstracts away how the test harness launches Jenkins-under-test and where,
 * which is determined at runtime by the user who runs the tests, not by the author
 * of tests.
 *
 * @author Vivek Pandey
 */
@ExtensionPoint
public abstract class JenkinsController implements Closeable, AutoCleaned {
    /**
     * directory on the computer where this code is running that points to a directory
     * where test code can place log files, cache files, etc.
     * Note that this directory might not exist on the Jenkins master, since it can be
     * running on a separate computer.
     */
    protected static final String WORKSPACE = System.getenv("WORKSPACE") != null
            ? System.getenv("WORKSPACE")
            : new File(System.getProperty("user.dir"), "target").getPath();

    protected static final String JENKINS_DEBUG_LOG = WORKSPACE + "/last_test.log";

    private boolean isRunning;

    protected final OutputStream logger;

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
     * Called when {@link JenkinsController} is pulled into a world prior to {@link #start()}
     */
    public void postConstruct(Injector injector) {
        injector.injectMembers(this);
    }

    /**
     * Starts Jenkins.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        if(!isRunning) {
            startNow();
            this.isRunning = true;
        }
    }

    /**
     * Synchronously start Jenkins instance until it starts responding to {@linkplain #getUrl() the specified URL}.
     */
    public abstract void startNow() throws IOException;


    /**
     * Stops Jenkins
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        if(isRunning) {
            stopNow();
        }
    }

    /**
     * Synchronously shutdown Jenkins instance.
     *
     * <p>
     * This method must leave JENKINS_HOME intact so that it can be {@linkplain #start() started} later.
     * To really delete the data and clean up, see {@link #tearDown()}.
     */
    public abstract void stopNow() throws IOException;

    /**
     * Alias for {@link #tearDown()}.
     */
    public final void close() throws IOException {
        stop();
        tearDown();
    }

    /**
     * Assuming the instance had already {@linkplain #stop() stopped}, destroy JENKINS_HOME and release resources
     * used by Jenkins.
     */
    public abstract void tearDown() throws IOException;


    /**
     * Stops and starts running Jenkins to perform a full JVM restart.
     *
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

    /**
     * Perform controller specific diagnostics for test failure. Defaults to no-op.
     * @param cause Failure cause
     */
    public void diagnose(Throwable cause) throws IOException {}

    /**
     * Populates the Jenkins Home with the specified template (which can be either a ZIP file or a directory). If
     * Jenkins is already running then it will be restarted.
     *
     * @param template The template (either a ZIP file or a directory).
     */
    public void populateJenkinsHome(File template) throws IOException {
        populateJenkinsHome(template, true);
    }

    /**
     * Populates the Jenkins Home with the specified template (which can be either a ZIP file or a directory). If
     * Jenkins is already running then it will be restarted.
     *
     * @param template The template (either a ZIP file or a directory).
     * @param clean    if {@code true} then the home will be wiped clean before the template is applied. If false then
     *                 the template will simply overwrite the existing (if any) home.
     */
    public abstract void populateJenkinsHome(File template, boolean clean) throws IOException;

    /**
     * Downloads the latest version of the form-element-path plugin that we use for testing.
     *
     * @deprecated is automatically resolved for each {@link JenkinsController}.
     */
    @Deprecated
    public static File downloadPathElement() {
        String source = "http://updates.jenkins-ci.org/latest/form-element-path.hpi";
        File target = new File(WORKSPACE,"path-element.hpi");
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
}
