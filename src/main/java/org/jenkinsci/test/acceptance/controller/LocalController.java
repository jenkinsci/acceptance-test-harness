package org.jenkinsci.test.acceptance.controller;

import java.util.logging.Level;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    protected final File jenkinsHome;

    protected ProcessInputStream process;

    protected JenkinsLogWatcher logWatcher;

    private final Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            process.getProcess().destroy();
        }
    };

    private final File logFile;

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
            jenkinsHome = Files.createTempDirectory(new File(WORKSPACE).toPath(), "jenkins" + "home").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a temp file",e);
        }

        this.logFile = new File(this.jenkinsHome.getParentFile(), this.jenkinsHome.getName()+".log");
    }

    @Override
    public void postConstruct(Injector injector) {
        super.postConstruct(injector);

        File pluginDir = new File(jenkinsHome,"plugins");
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
     * @deprecated Will not work correctly in Jenkins 2.33 and later. Apparently unused anyway.
     */
    @Deprecated
    public File getSlaveJarPath() {
        return new File(getJenkinsHome(),"war/WEB-INF/slave.jar"); // TODO look for war/WEB-INF/lib/remoting-*.jar instead
    }


    public File getJenkinsHome() {
        return jenkinsHome;
    }

    @Override
    public void populateJenkinsHome(byte[] _template, boolean clean) throws IOException {
        try {
            if (clean && jenkinsHome.isDirectory()) {
                FileUtils.cleanDirectory(jenkinsHome);
            }
            if (!jenkinsHome.isDirectory() && ! jenkinsHome.mkdirs()) {
                throw new IOException("Could not create directory: " + jenkinsHome);
            }
            File template = File.createTempFile("template", ".dat");
            try {
                FileUtils.writeByteArrayToFile(template, _template);
                Expand expand = new Expand();
                expand.setSrc(template);
                expand.setOverwrite(true);
                expand.setDest(jenkinsHome);
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
            onReady();
            LOGGER.info("Jenkins is running in " + this);
        } catch (Exception e) {
            diagnoseFailedLoad(e);
        }
    }

    /**
     * Called when the Jenkins instance is ready to be used.
     */
    protected void onReady() throws IOException {}

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
            if (jenkinsHome.exists()) {
                FileUtils.forceDelete(jenkinsHome);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cleaning up temporary JENKINS_HOME failed, retrying in 5 sec.", e);
            //maybe process is shutting down, wait for a sec then try again
            try {
                Thread.sleep(5000);
                if (jenkinsHome.exists()) {
                    FileUtils.forceDelete(jenkinsHome);
                }
            } catch (InterruptedException | IOException e1) {
                LOGGER.log(Level.WARNING, "Cleaning up temporary JENKINS_HOME failed again, giving up.");
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Common environment variables to put to {@link CommandBuilder} when launching Jenkins.
     */
    protected @NonNull Map<String, String> commonLaunchEnv() {
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

    private void diagnoseFailedLoad(Exception cause) throws IOException {
        String td = getThreaddump();
        if (td != null) {
            IOException ex = new IOException(cause.getMessage() + "\n\n" + td);
            ex.setStackTrace(cause.getStackTrace());
            cause = ex;
        }

        // Copy log to diagnostics
        FailureDiagnostics diagnostics = injector.getInstance(FailureDiagnostics.class);
        File log = diagnostics.touch("jenkins.log");
        Files.copy(logFile.toPath(), log.toPath(), StandardCopyOption.REPLACE_EXISTING);

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
        } catch (IllegalThreadStateException ignored) {
            // Process alive
        }
        try {
            long pid = proc.pid();
            ThreadDumpRuntime runtime = new PidRuntimeFactory().fromProcess(pid);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            runtime.toString(printStream, ModelObject.Mode.MACHINE);
            printStream.close();
            return baos.toString();
        } catch (UnsupportedOperationException ignored) {
            // ignored
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
        return null;
    }

    /**
     * Hostname to use when accessing Jenkins.
     * <p>
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
     * @param runInstallWizard - {@code true} to run the install wizard
     */
    public void setRunInstallWizard(boolean runInstallWizard) {
        this.runInstallWizard = runInstallWizard;
    }

    private static final Logger LOGGER = Logger.getLogger(LocalController.class.getName());
}
