package org.jenkinsci.test.acceptance.controller;

import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

/**
 * @author Vivek Pandey
 */
public class RemoteJenkinsController extends JenkinsController {

    private final Machine machine;
    private final String jenkinsHome;
    private final int httpPort;
    private final int controlPort;
    private LogWatcher logWatcher;
    private final String jenkinsWarLocation;
    protected ProcessInputStream process;

    public RemoteJenkinsController(Machine machine, String jenkinsHome, String jenkinsWar) {
        this.machine = machine;
        this.jenkinsHome = jenkinsHome;
        this.jenkinsWarLocation = jenkinsWar;
        this.httpPort = machine.getNextAvailablePort();
        this.controlPort = machine.getNextAvailablePort();
    }

    @Override
    public void startNow() throws IOException {
        CommandBuilder cb = new CommandBuilder("ssh", "-t",String.format("%s@%s",machine.getUser(),machine.getPublicIpAddress())).add(
                " java -DJENKINS_HOME=" + jenkinsHome +
                " -jar " + jenkinsWarLocation +
                " --ajp13Port=-1" +
                " --controlPort=" + controlPort +
                " --httpPort=" + httpPort);
        this.process =  cb.popen();
        this.logWatcher = new LogWatcher(process, logger, Collections.EMPTY_MAP);
        try {
            this.logWatcher.waitTillReady(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopNow() throws IOException {

        process.getProcess().destroy();
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
    public void diagnose() {
        throw new UnsupportedOperationException("Not implemented yet");
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
