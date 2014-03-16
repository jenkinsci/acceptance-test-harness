package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.controller.SshKeyPair;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;
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
    public void close() throws IOException {
        executor.shutdown();
        executor.shutdownNow();
    }

    private class SlaveInstaller implements Callable<Slave> {

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

            return create(machine.getPublicIpAddress());
        }

        public Slave create(String host) {
            // Just to make sure the dumb slave is set up properly, we should seed it
            // with a FS root and executors
            final DumbSlave s = j.slaves.create(DumbSlave.class);

            find(by.input("_.host")).sendKeys(host);
            s.save();

            // Fire the slave up before we move on
            waitForCond(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return s.isOnline();
                }
            }, 300);

            return s;

        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SshSlaveController.class);
}
