package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jenkinsci.test.acceptance.SshKeyPair;
import org.jenkinsci.test.acceptance.machine.Machine;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class SshSlaveController extends SlaveController {
    private final Machine machine;
    private final SshKeyPair keyPair;
    private final int slaveReadyTimeOutInSec;
    final AtomicBoolean slaveWaitComplete = new AtomicBoolean(false);
    private final String fingerprint;

    @Inject
    public SshSlaveController(Machine machine, SshKeyPair keyPair, @Named("slaveReadyTimeOutInSec") int slaveReadyTimeOutInSec) {
        this.machine = machine;
        this.keyPair = keyPair;
        String fingerprint;
        try {
            fingerprint = keyPair.getFingerprint();
        } catch (IOException e) {
            fingerprint = null;
        } catch (GeneralSecurityException e) {
            fingerprint = null;
        }
        this.fingerprint = fingerprint;
        this.slaveReadyTimeOutInSec = slaveReadyTimeOutInSec;
    }

    @Override
    public Future<Slave> install(Jenkins j) {
        ManagedCredentials credential = new ManagedCredentials(j);

        try {
            credential.open();
            if (credential.getElement(By.xpath(String.format("//input[@name='_.username'][@value='%s']"
                    +"/../../..//input[@name='_.description'][@value='%s']", machine.getUser(), fingerprint))) == null) {
                SshPrivateKeyCredential sc = credential.add(SshPrivateKeyCredential.class);
                sc.username.set(machine.getUser());
                sc.description.set(fingerprint);
                sc.selectEnterDirectly().privateKey.set(keyPair.readPrivateKey());
                credential.save();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        final Slave s = create(machine.getPublicIpAddress(), j);

        //Slave is configured, now wait till its online
        return new Future<Slave>(){

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return slaveWaitComplete.get();
            }

            @Override
            public boolean isDone() {
                return slaveWaitComplete.get() || s.isOnline();
            }

            @Override
            public Slave get() throws InterruptedException, ExecutionException {
                waitForOnLineSlave(s, slaveReadyTimeOutInSec);
                return s;
            }

            @Override
            public Slave get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if(unit != TimeUnit.SECONDS){
                    timeout = unit.toSeconds(timeout);
                }
                waitForOnLineSlave(s, (int) timeout);
                return s;

            }
        };
    }

    @Override
    public void close() throws IOException {
        //exit from wait if any
        if(!slaveWaitComplete.get()){
            slaveWaitComplete.set(true);
            sleep(1000);
        }
        stop();
        machine.close();
    }

    private void waitForOnLineSlave(final Slave s, int timeout){
        logger.info(String.format("Wait for the new slave %s to come online in %s seconds",machine.getId(), timeout));
        try {
            long endTime = System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(timeout);
            while (System.currentTimeMillis()<endTime) {
                if(s.isOnline()){
                    slaveWaitComplete.set(true);
                    return;
                }
                sleep(1000);
            }
            throw new org.openqa.selenium.TimeoutException(String.format("Slave could not be online in %s seconds",timeout));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new Error(String.format("An exception occurred while waiting for slave to be online in %s seconds",timeout),e);
        }
    }

    private Slave create(String host, Jenkins j) {
        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = j.slaves.create(DumbSlave.class);

        s.find(by.input("_.host")).sendKeys(host);

        s.waitFor(s.by.option(String.format("%s (%s)", machine.getUser(), fingerprint)));

        s.save();
        return s;

    }

    private static final Logger logger = LoggerFactory.getLogger(SshSlaveController.class);
}
