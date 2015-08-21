package org.jenkinsci.test.acceptance.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jenkinsci.test.acceptance.guice.AutoCleaned;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import com.google.inject.Injector;

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
@ExtensionPoint // TODO is it not the JenkinsControllerFactory that is the extension point?
public abstract class JenkinsController implements IJenkinsController, AutoCleaned {
    /**
     * directory on the computer where this code is running that points to a directory
     * where test code can place log files, cache files, etc.
     * Note that this directory might not exist on the Jenkins master, since it can be
     * running on a separate computer.
     */
    protected static final String WORKSPACE = System.getenv("WORKSPACE") != null
            ? System.getenv("WORKSPACE")
            : new File(System.getProperty("user.dir"), "target").getPath();

    public static final int STARTUP_TIMEOUT;
    static {
        int val = 360;
        String envvar = System.getenv("STARTUP_TIME");
        if (envvar != null) {
            int timeout = Integer.parseInt(envvar);
            if (timeout > 0) {
                val = timeout;
            }
        }
        STARTUP_TIMEOUT = val;
    }

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
    @Override
    public void start() throws IOException {
        if (!isRunning) {
            URL url = JenkinsController.class.getResource("/tool_installers.zip");
            if (url == null) {
                throw new RuntimeException(
                        "You need to run 'mvn generate-resources' before you can start test cases.\n"
                                + "Starting the Jenkins server under test requires that the tools configuration\n"
                                + "is provided in file tool_installers.zip in your class path.");
            }
            populateJenkinsHome(IOUtils.toByteArray(url), false);
            startNow();
            isRunning = true;
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
    @Override
    public void stop() throws IOException {
        if (isRunning) {
            stopNow();
            isRunning = false;
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
    @Override
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
    @Override
    public abstract URL getUrl();

    /**
     * Returns the short ID used to prefix log output from the process into the test.
     */
    public String getLogId() {
        return String.format("master%05d",getUrl().getPort());
    }

    /**
     * Perform controller specific diagnostics for test failure. Defaults to no-op.
     * @param cause Failure cause
     */
    public void diagnose(Throwable cause) throws IOException {}
}
