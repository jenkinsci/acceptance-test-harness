package org.jenkinsci.test.acceptance.machines;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.Ssh;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public class MultiTenantMachine implements Machine {

    private final Machine base;
    private final String dir;
    private final String id;
    private final MultitenancyMachineProvider provider;

    @Inject
    public MultiTenantMachine(MultitenancyMachineProvider provider, Machine machine) {
        this.base = machine;
        this.provider = provider;
        String mtSuffix = String.format("mt_%s", JcloudsMachine.newDirSuffix());
        this.dir = String.format("%s%s/",machine.dir(), mtSuffix);
        Ssh ssh = connect();
        ssh.executeRemoteCommand("mkdir -p "+this.dir);
        this.id = String.format("%s/%s",machine.getId(), mtSuffix);
    }

    @Override
    public String getId() {
        return id;
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
        provider.offer(this);
    }

}
