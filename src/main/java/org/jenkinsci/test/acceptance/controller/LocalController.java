package org.jenkinsci.test.acceptance.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.Expand;
import org.codehaus.plexus.util.StringUtils;
import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

    protected JenkinsLogWatcher logWatcher;

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
            String jenkinsWar = getenv("JENKINS_WAR");
            File warFile = firstExisting(false, jenkinsWar, WORKSPACE + "/jenkins.war", "./jenkins.war");
            if (warFile == null || !warFile.isFile()) {
                if (StringUtils.isBlank(jenkinsWar)) {
                    throw new RuntimeException(
                            "Could not find jenkins.war, maybe you forgot to set JENKINS_WAR env var?");
                }
                throw new RuntimeException(
                        "jenkins.war doesn't exist in " + jenkinsWar
                                + ", maybe you forgot to set JENKINS_WAR env var?");
            }
            return warFile;
        }

        protected final File firstExisting(boolean directory, String... candidatePaths) {
            for (String path: candidatePaths) {
                if (path == null) continue;
                File f = new File(path);
                if (directory ? f.isDirectory() : f.isFile()) {
                    return f;
                }
            }
            return null;
        }

    }

    /**
     * @param war
     *      Where is the jenkins.war file to be tested?
     */
    protected LocalController(File war) {
        this.war = war;
        if (!war.exists())
            throw new RuntimeException("Invalid path to jenkins.war specified: "+war);

        try {
            this.tempDir = File.createTempFile("jenkins", "home", new File(WORKSPACE));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a temp file",e);
        }

        this.logFile = new File(this.tempDir.getParentFile(), this.tempDir.getName()+".log");

        File formPathElement = downloadPathElement();
        File pluginDir = new File(tempDir,"plugins");
        pluginDir.mkdirs();

        File givenPluginDir = null;
        for (String d : Arrays.asList(
                getenv("PLUGINS_DIR"),
                new File(war.getParentFile(), "plugins").getAbsolutePath(),
                WORKSPACE + "/plugins",
                "plugins")) {
            if (d == null) {
                continue;
            }
            givenPluginDir = new File(d);
            if (givenPluginDir.isDirectory()) {
                break;
            }
        }

        if (givenPluginDir != null && givenPluginDir.isDirectory()) {
            try {
                File[] plugins = givenPluginDir.listFiles();
                if (plugins == null) {
                    throw new IOException("Plugin dir not readable");
                }
                for (File plugin : plugins) {
                    FileUtils.copyFileToDirectory(plugin, pluginDir);
                }
            } catch (IOException e) {
                String msg = String.format("Failed to copy plugins from %s to %s", givenPluginDir, pluginDir);
                throw new RuntimeException(msg, e);
            }
        }

        try {
            FileUtils.copyFile(formPathElement, new File(pluginDir,"path-element.hpi"));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy form path element file %s to plugin dir %s.", formPathElement, pluginDir),e);
        }
    }

    /**
     * @deprecated
     *      Use {@link #getJenkinsHome()}, which explains the nature of the directory better.
     */
    @Deprecated
    public File getTempDir() {
        return tempDir;
    }

    public File getSlaveJarPath() {
        return new File(getJenkinsHome(),"war/WEB-INF/slave.jar");
    }


    public File getJenkinsHome(){
        return tempDir;
    }

    @Override
    public void populateJenkinsHome(File template, boolean clean) throws IOException {
        boolean running = isRunning();
        try {
            stop();
            if (clean && tempDir.isDirectory()) {
                FileUtils.cleanDirectory(tempDir);
            }
            if (!tempDir.isDirectory() && ! tempDir.mkdirs()) {
                throw new IOException("Could not create directory: " + tempDir);
            }
            if (template.isDirectory()) {
                FileUtils.copyDirectory(template, tempDir);
            } else if (template.isFile()) {
                Expand expand = new Expand();
                expand.setSrc(template);
                expand.setOverwrite(true);
                expand.setDest(tempDir);
                expand.execute();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            if (running && !isRunning()) {
                start();
            }
        }
    }

    public File getJavaHome() {
        String javaHome = getenv("JENKINS_JAVA_HOME");
        File home = StringUtils.isBlank(javaHome) ? null : new File(javaHome);
        if (home != null && home.isDirectory()) {
            return home;
        }
        javaHome = getenv("JAVA_HOME");
        home = StringUtils.isBlank(javaHome) ? null : new File(javaHome);
        if (home != null && home.isDirectory()) {
            return home;
        }
        javaHome = System.getProperty("java.home");
        home = StringUtils.isBlank(javaHome) ? null : new File(javaHome);
        if (home != null && home.isDirectory()) {
            return home;
        }
        return null;
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
    public void diagnose(Throwable cause) {
        try {
            cause.printStackTrace(out);
            if(getenv("INTERACTIVE") != null && getenv("INTERACTIVE").equals("true")){
                out.println("Commencing interactive debugging. Browser session was kept open.");
                out.println("Press return to proceed.");
                in.read();
            }else{
                out.println("It looks like the test failed/errored, so here's the console from Jenkins:");
                out.println("--------------------------------------------------------------------------");
                out.println(FileUtils.readFileToString(logFile));
            }
        } catch (IOException e) {
            throw new Error(e);
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
            //maybe process is shutting down, wait for a sec then try again
            try {
                Thread.sleep(1000);
                FileUtils.forceDelete(tempDir);
            } catch (InterruptedException | IOException e1) {
                throw new RuntimeException(e);
            }

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

        logWatcher = new JenkinsLogWatcher(process,logFile);
        logWatcher.start();
        try {
            LOGGER.info("Waiting for Jenkins to become running in "+this);
            this.logWatcher.waitTillReady();
            LOGGER.info("Jenkins is running in "+this);
        } catch (Exception e) {
            diagnoseFailedLoad(e);
        }
    }

    private void diagnoseFailedLoad(Exception cause) {
        Process proc = process.getProcess();

        try {
            int val = proc.exitValue();
            new RuntimeException("Jenkins died loading. Exit code " + val, cause);
        } catch (IllegalThreadStateException _) {
            // Process alive
        }

        // Try to get stacktrace
        Class<?> clazz;
        Field pidField;
        try {
            clazz = Class.forName("java.lang.UNIXProcess");
            pidField = clazz.getDeclaredField("pid");
            pidField.setAccessible(true);
        } catch (Exception e) {
            LinkageError x = new LinkageError();
            x.initCause(e);
            throw x;
        }

        if (clazz.isAssignableFrom(proc.getClass())) {
            int pid;
            try {
                pid = (int) pidField.get(proc);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AssertionError(e);
            }

            try {
                Process jstack = new ProcessBuilder("jstack", String.valueOf(pid)).start();
                if (jstack.waitFor() == 0) {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(jstack.getInputStream(), writer);
                    RuntimeException ex = new RuntimeException(
                            cause.getMessage() + "\n\n" + writer.toString()
                    );
                    ex.setStackTrace(cause.getStackTrace());
                    throw ex;
                }
            } catch (IOException | InterruptedException e) {
                throw new AssertionError(e);
            }
        }

        throw new Error(cause);
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

    private static final Logger LOGGER = Logger.getLogger(LocalController.class.getName());
}
