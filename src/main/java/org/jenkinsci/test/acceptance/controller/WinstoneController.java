package org.jenkinsci.test.acceptance.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Splitter;
import com.google.inject.Injector;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import com.cloudbees.sdk.extensibility.Extension;

/**
 * Launches Jenkins via "java -jar jenkins.war" on the local machine.
 *
 * @author Vivek Pandey
 */
public class WinstoneController extends LocalController {

    private static final List<String> JAVA_OPTS;

    static {
        String opts = StringUtils.defaultString(System.getenv("JENKINS_JAVA_OPTS"));
        if (opts.isEmpty()) {
            JAVA_OPTS = null;
        } else {
            //Since we are only expecting opts in the form of "-Xms=XXm -Xmx=XXXm" we'll just do a simple split.
            JAVA_OPTS = Collections.unmodifiableList(
                    Splitter.onPattern("\\s+").splitToList(opts)
            );
        }
    }

    private final int httpPort;
    private final int controlPort;

    public WinstoneController() {
        httpPort = randomLocalPort();
        controlPort = randomLocalPort();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException{
        File javaHome = getJavaHome();
        String java = javaHome == null ? "java" : String.format("%s/bin/java",javaHome.getAbsolutePath());
        CommandBuilder cb = new CommandBuilder(java);
        if(JAVA_OPTS != null && !JAVA_OPTS.isEmpty()) {
            cb.addAll(JAVA_OPTS);
        }
        cb.add(
                "-Duser.language=en",
                "-jar", war,
                "--ajp13Port=-1",
                "--httpPort=" + httpPort);
        cb.env.putAll(commonLaunchEnv());
        System.out.println("Starting Jenkins: " + cb.toString());
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
}
