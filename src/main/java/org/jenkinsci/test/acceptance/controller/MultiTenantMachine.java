package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.resolver.JenkinsLinker;
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public class MultiTenantMachine implements Machine{

    private final Machine base;
    private final String dir;
    private final String jenkinsHome;

    @Inject
    public MultiTenantMachine(Machine machine) {
        this.base = machine;
        this.dir = String.format("%s/mt_%s",machine.dir(), JcloudsMachine.newDirSuffix());
        Ssh ssh = connect();
        ssh.executeRemoteCommand("mkdir -p "+this.dir);

        this.jenkinsHome = this.dir + "/jenkins.war";
        new JenkinsLinker(machine.jenkinsWarLocation()).materialize(this, this.jenkinsHome);
    }

    @Override
    public Ssh connect() {
        return base.connect();
    }

    @Override
    public String getPublicIpAddress() {
        return base.getPublicIpAddress();
    }

    @Override
    public String getUser() {
        return base.getUser();
    }

    @Override
    public String dir() {
        return dir;
    }

    @Override
    public int getNextAvailablePort() {
        return base.getNextAvailablePort();
    }

    @Override
    public void close() throws IOException {
        Ssh ssh = connect();
        //cleanup all directories for the next reuse
        ssh.executeRemoteCommand("rm -rf "+dir());
    }

    @Override
    public String jenkinsWarLocation() {
        return jenkinsHome;
    }
}
