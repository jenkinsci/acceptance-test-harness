package org.jenkinsci.test.acceptance.controller;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.Injector;

/**
 * Launches Jenkins via "java -jar jenkins.war" on the local machine.
 *
 * @author Vivek Pandey
 */
public class WinstoneController extends LocalController {

    private static final List<String> JENKINS_JAVA_OPTS = Arrays.asList(Optional.of(System.getenv("JENKINS_JAVA_OPTS")).orElse("").split("\\s+"));
    private static final List<String> JENKINS_OPTS = Arrays.asList(Optional.of(System.getenv("JENKINS_OPTS")).orElse("").split("\\s+"));

    private final int httpPort;
    private final int controlPort;

    @Inject
    public WinstoneController(Injector i) {
        super(i);
        httpPort = randomLocalPort();
        controlPort = randomLocalPort();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException{
        File javaHome = getJavaHome();
        String java = javaHome == null ? "java" : String.format("%s/bin/java",javaHome.getAbsolutePath());
        CommandBuilder cb = new CommandBuilder(java);
        cb.addAll(JENKINS_JAVA_OPTS);
        cb.add(
                "-Duser.language=en",
                "-jar", war,
                "--ajp13Port=-1",
                "--httpPort=" + httpPort
        );
        cb.addAll(JENKINS_OPTS);

        cb.env.putAll(commonLaunchEnv());
        LOGGER.info("Starting Jenkins: " + cb.toString());
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
