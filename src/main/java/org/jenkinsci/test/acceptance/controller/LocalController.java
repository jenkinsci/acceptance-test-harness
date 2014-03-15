package org.jenkinsci.test.acceptance.controller;

import org.apache.commons.io.input.TeeInputStream;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.*;

/**
 * Abstract base class for those JenkinsController that runs the JVM locally on
 * the same box as the test harness
 *
 * @author: Vivek Pandey
 */
public abstract class LocalController extends JenkinsController {
    /**
     * jenkins.war. Subject under test.
     */
    protected final File war;

    /**
     * JENKINS_HOME directory for jenkins.war to be launched.
     */
    protected final File tempDir;

    protected ProcessInputStream process;

    protected LogWatcher logWatcher;

    private static final Map<String,String> options = new HashMap<>();

    private final Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            process.getProcess().destroy();
        }
    };

    private final File logFile;

    static{
        String warLocation = getenv("JENKINS_WAR");
        if(warLocation == null){
            warLocation = WORKSPACE;
        }
        options.put("JENKINS_WAR", warLocation);
        String silent = getenv("silent") != null && getenv("silent").equalsIgnoreCase("true") ? "true" : "false";
        options.put("silent", silent);
        options.put("pattern", getenv("pattern"));
        options.put("log_pattern", getenv("log_pattern"));
    }

    /**
     * Partial implementation of {@link JenkinsControllerFactory} for subtypes.
     */
    public static abstract class LocalFactoryImpl implements JenkinsControllerFactory {
        /**
         * Determines the location of the war file.
         */
        protected File getWarFile() {
            String war = defaultsTo(getenv("JENKINS_WAR"), WORKSPACE + "/jenkins.war");

            File warfile = new File(war);
            if (!warfile.exists())
                throw new RuntimeException("jenkins.war doesn't exist in " + war + ", maybe you forgot to set JENKINS_WAR env var?");
            return warfile;
        }

        protected final String defaultsTo(String v, String w) {
            if (v==null)    v = w;
            return v;
        }
    }

    /**
     * @param war
     *      Where is the jenkins.war file to be tested?
     */
    protected LocalController(File war) {
        this.war = war;

        this.tempDir = FileUtils.createTempFile("jenkins", "home",new File(WORKSPACE));

        this.logFile = new File(this.tempDir.getName()+".log");

        File formPathElement = downloadPathElement();
        File pluginDir = new File(tempDir,"plugins");
        pluginDir.mkdirs();

        if(getenv("PLUGINS_DIR") != null){
            String givenPluginDir = getenv("PLUGINS_DIR");

            try {
                FileUtils.copyDirectory(new File(givenPluginDir), pluginDir,"/*.[hj]pi",null);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to copy plugins from %s to %s", pluginDir, givenPluginDir));
            }
        }

        try {
            FileUtils.copyFile(formPathElement, new File(pluginDir,"path-element.hpi"));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy form path element file %s to plugin dir %s.", formPathElement, pluginDir));
        }
    }

    /**
     * @deprecated
     *      Use {@link #getJenkinsHome()}, which explains the nature of the directory better.
     */
    public File getTempDir() {
        return tempDir;
    }

    public File getSlaveJarPath() {
        return new File(getJenkinsHome(),"war/WEB-INF/slave.jar");
    }


    public File getJenkinsHome(){
        return tempDir;
    }

    public abstract ProcessInputStream startProcess() throws IOException;

    @Override
    public void startNow() throws IOException{
        bringItUp();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopNow() throws IOException{
        process.getProcess().destroy();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    @Override
    public void diagnose() {
        if(getenv("INTERACTIVE") != null && getenv("INTERACTIVE").equals("true")){
            out.println("Commencing interactive debugging. Browser session was kept open.");
            out.println("Press return to proceed.");
            try {
                in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            out.println("It looks like the test failed/errored, so here's the console from Jenkins:");
            out.println("--------------------------------------------------------------------------");
            out.println(logWatcher.fullLog());
        }
    }

    @Override
    public void tearDown(){
        try {
            if (logger != null) {
                logger.close();
            }

            FileUtils.forceDelete(tempDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives random available port in the given range.
     *
     * @param from if <=0 then default value 49152 is used
     * @param to   if <=0 then default value 65535 is used
     */
    protected int randomLocalPort(int from, int to){
        from = (from <=0) ? 49152 : from;
        to = (to <= 0) ? 65535 : to;


        while(true){
            int candidate = (int) ((Math.random() * (to-from)) + from);
            if(isFreePort(candidate)){
                return candidate;
            }
            System.out.println(String.format("Port %s is in use", candidate));
        }
    }

    protected int randomLocalPort(){
        return randomLocalPort(-1,-1);
    }

    private void bringItUp() throws IOException{
        this.process = startProcess();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        this.logWatcher = new LogWatcher(new TeeInputStream(this.process,new FileOutputStream(logFile)), options);
        try {
            this.logWatcher.waitTillReady(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean  isFreePort(int port){
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
