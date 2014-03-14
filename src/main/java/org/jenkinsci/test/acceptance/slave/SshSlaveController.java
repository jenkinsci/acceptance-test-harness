package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.controller.SshKeyPair;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.SshSlave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class SshSlaveController extends SlaveController {
    private final Machine machine;
    private final SshKeyPair keyPair;

    @Inject
    public SshSlaveController(Machine machine, SshKeyPair keyPair) {
        this.machine = machine;
        this.keyPair = keyPair;
    }

    @Override
    public Slave install(Jenkins j) {

        SshPrivateKeyCredential credential = new SshPrivateKeyCredential(j);

        try {
            credential.create("GLOBAL",machine.getUser(),keyPair.readPrivateKey());
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        return SshSlave.create(j, machine.getPublicIpAddress());
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }

    @Override
    public void close() throws IOException {

    }

    private static final Logger logger = LoggerFactory.getLogger(SshSlaveController.class);
}
