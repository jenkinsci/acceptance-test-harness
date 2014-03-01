package org.jenkinsci.test.acceptance.controller;

import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: Vivek Pandey
 */
public class WinstoneController extends LocalController {

    private final int httpPort;
    private final int controlPort;

    public WinstoneController() {
        httpPort = randomLocalPort();
        controlPort = randomLocalPort();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException{
        CommandBuilder cb = new CommandBuilder("java","-DJENKINS_HOME="+getTempDir(), "-jar", warLocation,
                "--ajp13Port=-1", "--controlPort="+controlPort, "--httpPort="+httpPort, "2>&1");
        return cb.popen();
    }

    @Override
    public void stopNow() throws IOException{
        process.getProcess().destroy();
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(String.format("http://127.0.0.1:%s/",httpPort));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
