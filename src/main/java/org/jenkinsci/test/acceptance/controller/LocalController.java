package org.jenkinsci.test.acceptance.controller;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.Expand;
import org.codehaus.plexus.util.StringUtils;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.jenkinsci.test.acceptance.log.LogListenable;
import org.jenkinsci.test.acceptance.log.LogListener;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;
import org.junit.runners.model.MultipleFailureException;
import org.openqa.selenium.TimeoutException;

import com.github.olivergondza.dumpling.factory.PidRuntimeFactory;
import com.github.olivergondza.dumpling.model.ModelObject;
import com.github.olivergondza.dumpling.model.dump.ThreadDumpRuntime;
import com.google.inject.Injector;

import static java.lang.System.*;

/**
 * Abstract base class for those JenkinsController that runs the JVM locally on
 * the same box as the test harness
 *
 * @author Vivek Pandey
 */
public abstract class LocalController extends JenkinsController implements LogListenable {
    /**
     * jenkins.war. Subject under test.
     */
    @Inject @Named("jenkins.war")
    protected /*final*/ File war;

    /**
     * JENKINS_HOME directory for jenkins.war to be launched.
     */
    protected final File tempDir;

    protected ProcessInputStream process;

    protected JenkinsLogWatcher logWatcher;

    private final Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            process.getProcess().destroy();
        }
    };

    private final File logFile;

    @Inject @Named("form-element-path.hpi")
    private File formElementPathPlugin;

    @Inject
    private Injector injector;

    /**
     * Flag to indicate if the install wizard should be run
     */
    private boolean runInstallWizard = false;

    /**
     * Partial implementation of {@link JenkinsControllerFactory} for subtypes.
     */
    public static abstract class LocalFactoryImpl implements JenkinsControllerFactory {
    }

    protected LocalController(Injector i) {
        super(i);
        try {
            tempDir = File.createTempFile("jenkins", "home", new File(WORKSPACE));
            tempDir.delete();
            tempDir.mkdirs();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a temp file",e);
        }

        this.logFile = new File(this.tempDir.getParentFile(), this.tempDir.getName()+".log");
    }

    @Override
    public void postConstruct(Injector injector) {
        super.postConstruct(injector);

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
                FileUtils.copyDirectory(givenPluginDir, pluginDir);
            } catch (IOException e) {
                String msg = String.format("Failed to copy plugins from %s to %s", givenPluginDir, pluginDir);
                throw new RuntimeException(msg, e);
            }
        }

        LOGGER.info("Running with given plugins: " + Arrays.toString(pluginDir.list()));

        try {
            FileUtils.copyFile(formElementPathPlugin, new File(pluginDir, "path-element.hpi"));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy form path element file %s to plugin dir %s.",
                    formElementPathPlugin, pluginDir),e);
        }
    }

    @Override
    public void addLogListener(LogListener l) {
        logWatcher.addLogListener(l);
    }

    @Override
    public void removeLogListener(LogListener l) {
        logWatcher.removeLogListener(l);
    }

    /**
     * @deprecated
     *      Use {@link #getJenkinsHome()}, which explains the nature of the directory better.
     */
    @Deprecated
    public File getTempDir() {
        return tempDir;
    }

    /**
     * @deprecated Will not work correctly in Jenkins 2.33 and later. Apparently unused anyway.
     */
    @Deprecated
    public File getSlaveJarPath() {
        return new File(getJenkinsHome(),"war/WEB-INF/slave.jar"); // TODO look for war/WEB-INF/lib/remoting-*.jar instead
    }


    public File getJenkinsHome(){
        return tempDir;
    }

    @Override
    public void populateJenkinsHome(byte[] _template, boolean clean) throws IOException {
        try {
            if (clean && tempDir.isDirectory()) {
                FileUtils.cleanDirectory(tempDir);
            }
            if (!tempDir.isDirectory() && ! tempDir.mkdirs()) {
                throw new IOException("Could not create directory: " + tempDir);
            }
            File template = File.createTempFile("template", ".dat");
            try {
                FileUtils.writeByteArrayToFile(template, _template);
                Expand expand = new Expand();
                expand.setSrc(template);
                expand.setOverwrite(true);
                expand.setDest(tempDir);
                expand.execute();
            } finally {
                template.delete();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
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
    public void startNow() throws IOException {
        this.process = startProcess();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        logWatcher = new JenkinsLogWatcher(getLogId(),process,logFile, getLogPrinter());
        logWatcher.start();
        try {
            LOGGER.info("Waiting for Jenkins to become running in "+ this);
            this.logWatcher.waitTillReady();
            LOGGER.info("Jenkins is running in " + this);
        } catch (Exception e) {
            diagnoseFailedLoad(e);
        }
    }

    @Override
    public void stopNow() throws IOException{
        process.getProcess().destroy();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    @Override
    public void diagnose(Throwable cause) {

        if (cause instanceof TimeoutException) {
            FailureDiagnostics diagnostics = injector.getInstance(FailureDiagnostics.class);
            String td = getThreaddump();
            if (td != null) {
                diagnostics.write("threaddump.log", td);
            }
        }

        if (getenv("INTERACTIVE") != null && getenv("INTERACTIVE").equals("true")) {
            if (cause instanceof MultipleFailureException) {
                System.out.println("Multiple exceptions occurred:");
                for (Throwable c : ((MultipleFailureException) cause).getFailures()) {
                    out.println();
                    c.printStackTrace(out);
                    out.println();
                }
            } else {
                cause.printStackTrace(out);
            }
            out.println("Commencing interactive debugging. Browser session was kept open.");
            // Broken in current surefire
//            out.println("Press return to proceed.");
//            try {
//                new BufferedReader(new InputStreamReader(System.in)).readLine();
//            } catch (IOException e) {
//                throw new Error(e);
//            }

            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
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
            System.out.println("Cleaning up temporary JENKINS_HOME failed, retrying in 5 sec.");
            //maybe process is shutting down, wait for a sec then try again
            try {
                Thread.sleep(5000);
                FileUtils.forceDelete(tempDir);
            } catch (InterruptedException | IOException e1) {
                System.out.println("Cleaning up temporary JENKINS_HOME failed again, giving up.");
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * Common environment variables to put to {@link CommandBuilder} when launching Jenkins.
     */
    protected @Nonnull Map<String, String> commonLaunchEnv() {
        HashMap<String, String> env = new HashMap<>();
        env.put("JENKINS_HOME", getJenkinsHome().getAbsolutePath());
        File javaHome = getJavaHome();
        if (javaHome != null) {
            env.put("JAVA_HOME", javaHome.getAbsolutePath());
        }

        if (!runInstallWizard) {
            env.put("jenkins.install.state", "TEST");
        }

        return env;
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
            LOGGER.info(String.format("Port %s is in use", candidate));
        }
    }

    protected int randomLocalPort(){
        return randomLocalPort(-1,-1);
    }

    private void diagnoseFailedLoad(Exception cause) throws IOException {
        String td = getThreaddump();
        if (td != null) {
            IOException ex = new IOException(cause.getMessage() + "\n\n" + td);
            ex.setStackTrace(cause.getStackTrace());
            cause = ex;
        }

        throw (cause instanceof IOException)
                ? (IOException) cause
                : new IOException("Jenkins failed to load", cause)
        ;
    }

    private @CheckForNull String getThreaddump() {
        Process proc = process.getProcess();

        try {
            proc.exitValue();
            return null; // already dead
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
            return null; // Unable to inspect
        }

        if (clazz.isAssignableFrom(proc.getClass())) {
            int pid;
            try {
                pid = (int) pidField.get(proc);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AssertionError(e);
            }

            try {
                ThreadDumpRuntime runtime = new PidRuntimeFactory().fromProcess(pid);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream(baos);
                runtime.toString(printStream, ModelObject.Mode.MACHINE);
                printStream.close();
                return baos.toString();
            } catch (IOException | InterruptedException e) {
                throw new AssertionError(e);
            }
        }

        return null;
    }

    private boolean isFreePort(int port){
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Hostname to use when accessing Jenkins.
     *
     * Useful to override with public hostname/IP when external clients needs to talk back to Jenkins.
     *
     * @return "127.0.0.1" unless overridden via JENKINS_LOCAL_HOSTNAME env var.
     */
    protected String getSutHostName() {
        String name = System.getenv("JENKINS_LOCAL_HOSTNAME");
        if (name == null || name.isEmpty()) {
            name = "127.0.0.1";
        }
        return name;
    }

    /**
     * @return true if the install wizard is going to be run
     */
    public boolean isRunInstallWizard() {
        return runInstallWizard;
    }

    /**
     * Set the flag to run the install wizard.
     * 
     * @param runInstallWizard - <code>true</code> to run the install wizard
     */
    public void setRunInstallWizard(boolean runInstallWizard) {
        this.runInstallWizard = runInstallWizard;
    }

    private static final Logger LOGGER = Logger.getLogger(LocalController.class.getName());
}
