package org.jenkinsci.test.acceptance.slave;

import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class SshSlaveController extends SlaveController {
    private final Machine machine;

    public SshSlaveController(Machine machine) {
        this.machine = machine;
    }

    @Override
    public Slave install(Jenkins j) {
        // TODO: use page object to create a new SSH slave on 'j'
        throw new UnsupportedOperationException(); // TO BE IMPLEMENTED
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
}
