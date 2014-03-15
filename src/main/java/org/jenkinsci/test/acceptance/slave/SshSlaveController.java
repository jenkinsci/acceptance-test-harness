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

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class SshSlaveController extends SlaveController {
    private final Machine machine;
    private final SshKeyPair keyPair;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @Inject
    public SshSlaveController(Machine machine, SshKeyPair keyPair) {
        this.machine = machine;
        this.keyPair = keyPair;
    }

    @Override
    public Future<Slave> install(Jenkins j) {
        return executor.submit(new SlaveInstaller(j));
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
        executor.shutdown();
        executor.shutdownNow();
    }

    private class SlaveInstaller implements Callable<Slave>{

        private final Jenkins j;

        private SlaveInstaller(Jenkins j) {
            this.j = j;
        }
        @Override
        public Slave call() throws Exception {
            SshPrivateKeyCredential credential = new SshPrivateKeyCredential(j);

            try {
                credential.create("GLOBAL",machine.getUser(),keyPair.readPrivateKey());
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            return SshSlave.create(j, machine.getPublicIpAddress());
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SshSlaveController.class);
}
