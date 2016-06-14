package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jenkinsci.test.acceptance.SshKeyPair;
import org.jenkinsci.test.acceptance.machine.Machine;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.Callable;
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
            if (credential.checkIfCredentialsExist(fingerprint) == null) {
                CredentialsPage cp = new CredentialsPage(j, ManagedCredentials.DEFAULT_DOMAIN);
                SshPrivateKeyCredential sc = cp.add(SshPrivateKeyCredential.class);
                sc.username.set(machine.getUser());
                sc.description.set(fingerprint);
                sc.selectEnterDirectly().privateKey.set(keyPair.readPrivateKey());
                cp.create();
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
            elasticSleep(1000);
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
                elasticSleep(1000);
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

        elasticSleep(25);

        final Select cId = new Select(s.find(by.input("_.credentialsId")));
        final String credentialName = String.format("%s (%s)", machine.getUser(), fingerprint);
        waitFor().until(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                List<WebElement> options = cId.getOptions();
                for (WebElement e: options) {
                    if (credentialName.equals(e.getText())) return true;
                }
                return false;
            }
        });
        cId.selectByVisibleText(credentialName);
        s.setExecutors(1);

        s.save();
        return s;

    }

    private static final Logger logger = LoggerFactory.getLogger(SshSlaveController.class);
}
