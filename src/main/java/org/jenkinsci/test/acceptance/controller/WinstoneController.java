package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

/**
 * Launches Jenkins via "java -jar jenkins.war" on the local machine.
 *
 * @author Vivek Pandey
 */
public class WinstoneController extends LocalController {

    private static final List<String> JENKINS_JAVA_OPTS = envVarOpts("JENKINS_JAVA_OPTS");
    private static final List<String> JENKINS_OPTS = envVarOpts("JENKINS_OPTS");
    // options to the JVM that can be added on a testcase basis in code
    private final List<String> JAVA_OPTS = new ArrayList<>();

    private File portFile;

    protected static List<String> envVarOpts(String jenkins_opts) {
        String getenv = System.getenv(jenkins_opts);
        if (getenv == null) return Collections.emptyList();
        return Arrays.asList(getenv.split("\\s+"));
    }

    public void addJavaOpt(String javaOpt) {
        if (StringUtils.isNotBlank(javaOpt)) {
            JAVA_OPTS.add(javaOpt);
        }
    }

    protected int httpPort;

    @Inject
    public WinstoneController(Injector i) {
        this(i, 0);
    }

    public WinstoneController(Injector i, int httpPort) {
        super(i);
        this.httpPort = httpPort;
    }

    @Override
    protected void onReady() throws IOException {
        if (this.httpPort == 0 && portFile != null) {
            String s = FileUtils.readFileToString(portFile, StandardCharsets.UTF_8);
            try {
                this.httpPort = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                // Should not happen assuming this is called after a successful start.
                throw new IOException("Unable to parse port from " + s + ". Jenkins did not start.");
            }
        }
        super.onReady();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException{
        File javaHome = getJavaHome();
        String java = javaHome == null ? "java" : String.format("%s/bin/java",javaHome.getAbsolutePath());
        CommandBuilder cb = new CommandBuilder(java);
        cb.addAll(JENKINS_JAVA_OPTS);
        cb.addAll(JAVA_OPTS);
        cb.add("-Duser.language=en",
                "-Djenkins.formelementpath.FormElementPathPageDecorator.enabled=true");
        portFile = File.createTempFile("jenkins-port", ".txt");
        portFile.deleteOnExit();
        cb.add("-Dwinstone.portFileName=" + portFile.getAbsolutePath());
        cb.add("-jar", war,
                "--httpPort=" + httpPort
        );
        cb.addAll(JENKINS_OPTS);

        cb.env.putAll(commonLaunchEnv());
        LOGGER.info("Starting Jenkins: " + cb);
        return cb.popen();
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(String.format("http://" + getSutHostName() + ":%s/",httpPort));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getLogId() {
        // as we will generally start with using a port of `0` we can not use the base classes implementation
        // as the logging id will change through the lifecycle and not be initialized from the outset
        return String.format("master%08d",  System.identityHashCode(this));
    }

    @Override
    public String toString() {
        return getUrl().toExternalForm();
    }

    @Extension
    public static class FactoryImpl extends LocalFactoryImpl {
        @Inject Injector i;

        @Override
        public String getId() {
            return "winstone";
        }

        @Override
        public JenkinsController create() {
            return i.getInstance(WinstoneController.class);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(WinstoneController.class.getName());
}
