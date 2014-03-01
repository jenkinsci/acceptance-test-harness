package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.Extension;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: Vivek Pandey
 */
@Extension
public class WinstoneController extends LocalController {

    private final int httpPort;
    private final int controlPort;

    public WinstoneController() {
        httpPort = randomLocalPort();
        controlPort = randomLocalPort();
    }

    @Override
    public ProcessInputStream startProcess() {
        CommandBuilder cb = new CommandBuilder("exec", "java","-DJENKINS_HOME="+getTempDir(), "-jar", warLocation,
                "--ajp13Port=-1", "--controlPort="+controlPort, "--httpPort="+httpPort, "2>&1");
        try {
            return cb.popen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to launch Jenkins", e);
        }
    }

    @Override
    public void stopNow() {
        process.getProcess().destroy();
    }

    @Override
    public URL getUrl() {
        try {
            return new URL("http://127.0.0.1:%"+httpPort);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
