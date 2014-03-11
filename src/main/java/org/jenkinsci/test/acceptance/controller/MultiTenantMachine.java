package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public class MultiTenantMachine implements Machine{

    private final Machine base;
    private final String dir;

    @Inject
    public MultiTenantMachine(Machine machine) {
        this.base = machine;
        this.dir = String.format("%s/mt_%s",machine.dir(), JcloudsMachine.newDirSuffix());
        Ssh ssh = connect();
        ssh.executeRemoteCommand("mkdir -p "+this.dir);
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

    public Machine baseMachine(){
        return base;
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
}
