package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
    private final int slaveReadyTimeOutInSec;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @Inject
    public SshSlaveController(Machine machine, SshKeyPair keyPair, @Named("slaveReadyTimeOutInSec") int slaveReadyTimeOutInSec) {
        this.machine = machine;
        this.keyPair = keyPair;
        this.slaveReadyTimeOutInSec = slaveReadyTimeOutInSec;
    }

    @Override
    public Future<Slave> install(Jenkins j) {
        SshPrivateKeyCredential credential = new SshPrivateKeyCredential(j);

        try {
            credential.create("GLOBAL",machine.getUser(),keyPair.readPrivateKey());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        final Slave s = create(machine.getPublicIpAddress(), j);

        //Slave is configured, now wait till its online
        return executor.submit(new Callable<Slave>() {
            @Override
            public Slave call() throws Exception {
                logger.info(String.format("Wait for the new slave %s to come online in %s seconds",machine.getId(), slaveReadyTimeOutInSec));
                waitForCond(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return s.isOnline();
                    }
                }, slaveReadyTimeOutInSec);
                return s;
            }
        });
    }

    @Override
    public void close() throws IOException {
        executor.shutdown();
        executor.shutdownNow();
    }

    private Slave create(String host, Jenkins j) {
        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = j.slaves.create(DumbSlave.class);

        s.find(by.input("_.host")).sendKeys(host);
        s.save();
        return s;

    }

    private static final Logger logger = LoggerFactory.getLogger(SshSlaveController.class);
}
