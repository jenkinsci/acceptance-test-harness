package org.jenkinsci.test.acceptance.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import com.cloudbees.sdk.extensibility.Extension;

/**
 * Launches Jenkins via "java -jar jenkins.war" on the local machine.
 *
 * @author Vivek Pandey
 */
public class WinstoneController extends LocalController {

    private final int httpPort;
    private final int controlPort;

    public WinstoneController(final File warFile) {
        super(warFile);

        httpPort = randomLocalPort();
        controlPort = randomLocalPort();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException{
        File javaHome = getJavaHome();
        ArrayList<String> proxyProperties = getProxyProperties();
        String java = javaHome == null ? "java" : String.format("%s/bin/java",javaHome.getAbsolutePath());
        CommandBuilder cb = new CommandBuilder(java).add("-DJENKINS_HOME=" + getJenkinsHome());
        for (String proxyProp: proxyProperties) {
            cb.add(proxyProp);
        }
        cb.add("-Duser.language=en");
        cb.add("-jar", war);
        cb.add("--ajp13Port=-1");
        cb.add("--httpPort=" + httpPort);
        cb.env.putAll(commonLaunchEnv());
        System.out.println("Starting Jenkins: " + cb.toString());
        return cb.popen();
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(String.format("http://127.0.0.1:%s/",httpPort));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Extension
    public static class FactoryImpl extends LocalFactoryImpl {
        @Override
        public String getId() {
            return "winstone";
        }

        @Override
        public JenkinsController create() {
            return new WinstoneController(getWarFile());
        }
    }
}
