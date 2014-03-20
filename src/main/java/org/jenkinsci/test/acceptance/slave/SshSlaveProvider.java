package org.jenkinsci.test.acceptance.slave;

import com.google.inject.name.Named;
import org.jenkinsci.test.acceptance.machine.Machine;
import org.jenkinsci.test.acceptance.machine.MachineProvider;
import org.jenkinsci.test.acceptance.SshKeyPair;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class SshSlaveProvider extends SlaveProvider {
    @Inject
    private MachineProvider provider;

    @Inject
    private SshKeyPair keyPair;

    @com.google.inject.Inject(optional = true)
    @Named("slaveReadyTimeOutInSec")
    private int slaveReadyTimeOutInSec = 300;

    @Override
    public SlaveController create() {
        // TODO: multi-tenant
        Machine m = provider.get();
        return new SshSlaveController(m,keyPair,slaveReadyTimeOutInSec);
    }
}
