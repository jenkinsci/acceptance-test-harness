package org.jenkinsci.test.acceptance.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Vivek Pandey
 */
public class RemoteJenkinsController extends JenkinsController {

    private final Machine machine;
    private final String jenkinsHome;
    private int pid;

    public RemoteJenkinsController(Machine machine) {
        this.machine = machine;
        this.jenkinsHome = newJenkinsHome(machine);
        try {
            Ssh ssh = new Ssh(machine.getNodeMetadata().getCredentials().getUser(), machine.getNodeMetadata().getPublicAddresses().iterator().next());
            ssh.executeRemoteCommand("mkdir " + jenkinsHome, logger);
            ssh.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void startNow() throws IOException {
        if(pid > 0){
            throw new RuntimeException("Jenkins instance is already running...");
        }
        Ssh ssh = new Ssh(machine.getNodeMetadata().getCredentials().getUser(), machine.getNodeMetadata().getPublicAddresses().iterator().next());
        this.pid = ssh.executeRemoteCommand(String.format("java -jar jenkins.war -DJENKINS_HOME=%s --ajp13Port=-1 --controlPort=%s --httpPort=%s", jenkinsHome, 8081, 8080), logger);
        ssh.destroy();
    }

    @Override
    public void stopNow() throws IOException {
        if(pid <= 0){
            throw new RuntimeException("Jenkins instance is not running...");
        }
        Ssh ssh = new Ssh(machine.getNodeMetadata().getCredentials().getUser(), machine.getNodeMetadata().getPublicAddresses().iterator().next());
        ssh.executeRemoteCommand(String.format("kill -INT "+pid), logger);
        ssh.destroy();
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(String.format("http://%s:%s/", machine.getNodeMetadata().getPublicAddresses().iterator().next(), "8080"));
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

    }

    private String newJenkinsHome(Machine machine){
        SecureRandom secureRandom = new SecureRandom();
        long secureInitializer = secureRandom.nextLong();
        Random rand = new Random( secureInitializer + Runtime.getRuntime().freeMemory() );
        return String.format("%s_%s",machine.getNodeMetadata().getGroup(), rand.nextInt());
    }

}
