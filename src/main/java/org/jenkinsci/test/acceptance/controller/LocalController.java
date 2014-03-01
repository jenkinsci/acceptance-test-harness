package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.ControllerException;
import org.jenkinsci.utils.process.ProcessInputStream;
import org.jenkinsci.utils.process.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.System.*;

/**
 * @author: Vivek Pandey
 */
public abstract class LocalController extends JenkinsController{

    protected final String warLocation;
    private final String tempDir;
    private final String formPathElement;
    protected ProcessInputStream process;
    private int pid;

    private static final Map<String,String> options = new HashMap<>();

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

    @Inject(optional=true)
    protected LocalController(String warLocation) {
        super(null);
        if(warLocation == null){
            warLocation = getenv("JENKINS_WAR");
            if(warLocation == null){
                warLocation = WORKSPACE;
            }
        }
        if(warLocation == null){
            throw new ControllerException("Need WAR file location. Please set environment varialbe JENKINS_WAR pointing to the Jenkins war location");
        }

        this.warLocation = warLocation;

        File tDir = FileUtils.createTempFile("temp", "dir",new File(WORKSPACE));
        this.tempDir = tDir.getAbsolutePath();
        this.formPathElement = downloadPathElement();
        String pluginDirPath = this.tempDir+"/plugins";
        File pluginDir = new File(pluginDirPath);
        pluginDir.mkdirs();

        if(getenv("PLUGINS_DIR") != null){
            String givenPluginDir = getenv("PLUGINS_DIR");

            try {
                FileUtils.copyDirectory(new File(givenPluginDir), pluginDir,"/*.[hj]pi",null);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to copy plugins from %s to %s", pluginDirPath, givenPluginDir));
            }
        }

        try {
            FileUtils.copyFile(new File(formPathElement), new File(pluginDirPath+File.separator+"path-element.hpi"));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy form path element file %s to plugin dir %s.", formPathElement, pluginDirPath));
        }
    }

    protected LocalController(){
        this(null);
    }

    @Override
    public String getTempDir() {
        return tempDir;
    }

    public String getJenkinsHome(){
        return tempDir;
    }

    public abstract ProcessInputStream startProcess() throws IOException;

    @Override
    public void startNow() throws IOException{
        out.println("\n    Bringing up a temporary Jenkins instance");
        bringItUp();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void diagnose() {
        if(getenv("INTERACTIVE") != null && getenv("INTERACTIVE") == "true"){
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
                logger = null;
            }

            FileUtils.forceDelete(tempDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives random availabe port in the given range.
     *
     * @param from if <=0 then default value 49152 is used
     * @param to   if <= 0 then default value 65535 is used
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
        this.pid = ProcessUtils.getPid(process.getProcess());
        this.logWatcher = new LogWatcher(this.process, logger, options);
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
