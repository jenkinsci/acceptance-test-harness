package org.jenkinsci.test.acceptance.controller;

import jnr.ffi.LibraryLoader;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.Ssh;
import org.jenkinsci.test.acceptance.machine.Machine;
import org.jenkinsci.test.acceptance.utils.GNUCLibrary;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;
import org.jenkinsci.utils.process.ProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.System.*;

/**
 * A {@link JenkinsController} that runs on a remote machine. It can be injected in tests using
 *
 * <pre>
 *      //groovy configuration
 *      bind MachineProvider to MultitenancyMachineProvider
 *      bind MachineProvider named "raw" to Ec2Provider
 *      bind RemoteJenkinsController toProvider JenkinsProvider
 *
 *      &#064;Inject
 *      private JenkinsController jenkinsController;
 * </pre>
 *
 * It resolves Jenkins and plugin installation using {@link org.jenkinsci.test.acceptance.resolver.JenkinsResolver}.
 * {@link #close()} also calls {@link org.jenkinsci.test.acceptance.machine.Machine#close()}.
 *
 * @author Vivek Pandey
 */
public class RemoteJenkinsController extends JenkinsController {

    private final Machine machine;
    private final String jenkinsHome;
    private final int httpPort;
    private final int controlPort;
    private JenkinsLogWatcher logWatcher;
    private final String jenkinsWarLocation;
    protected ProcessInputStream process;
    private final File logFile;
    private final File privateKeyLocation;

    public RemoteJenkinsController(Machine machine, String jenkinsHome, String jenkinsWar, File privateKeyLocation) {
        this.machine = machine;
        this.jenkinsHome = jenkinsHome;
        this.jenkinsWarLocation = jenkinsWar;
        this.logFile = new File(WORKSPACE, new File(jenkinsHome).getName()+"_log.log");
        this.httpPort = machine.getNextAvailablePort();
        this.controlPort = machine.getNextAvailablePort();
        this.privateKeyLocation = privateKeyLocation;
    }

    @Override
    public void startNow() throws IOException {
        CommandBuilder cb = new CommandBuilder("ssh","-i", privateKeyLocation.getAbsolutePath() , "-t", "-oStrictHostKeyChecking=no" ,String.format("%s@%s",machine.getUser(),machine.getPublicIpAddress())).add(
                "java -DJENKINS_HOME=" + jenkinsHome +
                " -jar " + jenkinsWarLocation +
                " --ajp13Port=-1" +
                " --controlPort=" + controlPort +
                " --httpPort=" + httpPort);
        localLogger.info("Launching Jenkins: "+cb);
        this.process =  cb.popen();

        /** Write to System.out so that JUnit Attachment Plugin catches up the log
         *  https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Attachments+Plugin
         **/
        System.out.println(String.format("[[ATTACHMENT|%s]]", logFile.getAbsolutePath()));

        logWatcher = new JenkinsLogWatcher(getLogId(), process, logFile);
        logWatcher.start();
        try {
            this.logWatcher.waitTillReady();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopNow() throws IOException {
        Process p = process.getProcess();
        int pid = ProcessUtils.getPid(p);

        localLogger.info(String.format("Killing Jenkins Process, pid: %s running at JENKINS_HOME: %s",pid, jenkinsHome));
        LibraryLoader.create(GNUCLibrary.class).load("c").kill(pid,2);
        synchronized (p) {
            try {
                p.wait(3000);
            } catch (InterruptedException e) {
                throw (IOException)new InterruptedIOException().initCause(e);
            }
        }
        p.destroy();    // if it hasn't died after 3 sec, just kill it
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(String.format("http://%s:%s/", machine.getPublicIpAddress(), httpPort));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void populateJenkinsHome(byte[] _template, boolean clean) throws IOException {
        boolean running = isRunning();
        try (Ssh connection = machine.connect()) {
            stop();
            if (clean) {
                connection.executeRemoteCommand("rm -rf "+ Ssh.escape(jenkinsHome) + "; mkdir -p " + Ssh.escape(jenkinsHome));
            }
            File template = File.createTempFile("template", ".dat");
            try {
                org.apache.commons.io.FileUtils.writeByteArrayToFile(template, _template);
                connection.copyTo(template.getAbsolutePath(), ".home-template.zip", jenkinsHome);
            } finally {
                template.delete();
            }
                String templateArchive =
                        Ssh.escape(jenkinsHome + (jenkinsHome.endsWith("/") ? "" : "/") + ".home-template.zip");
                connection.executeRemoteCommand(
                        "unzip -o " + templateArchive + " -d " + Ssh.escape(jenkinsHome) + " && rm -f "
                                + templateArchive);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            if (running && !isRunning()) {
                start();
            }
        }
    }

    @Override
    public void diagnose(Throwable cause) throws IOException {
        out.println("Error: "+cause.getMessage());
        cause.printStackTrace();
        out.println("It looks like there was an error, here's the console from Jenkins:");
        out.println("--------------------------------------------------------------------------");
        out.println(FileUtils.fileRead(logFile));
    }

    @Override
    public void tearDown() {
        try {
            machine.close();
        } catch (IOException e) {
            throw new AssertionError("Failed to clean machine");
        }

    }

    private static final Logger localLogger = LoggerFactory.getLogger(RemoteJenkinsController.class);



}
