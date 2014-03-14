package org.jenkinsci.test.acceptance.slave;

import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.controller.MachineProvider;
import org.jenkinsci.test.acceptance.controller.SshKeyPair;
import org.jenkinsci.test.acceptance.guice.TestCleaner;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class SshSlaveProvider implements SlaveProvider {
    @Inject
    private MachineProvider provider;

    @Inject
    private SshKeyPair keyPair;

    @Inject
    private Provider<TestCleaner> testCleaner;

    @Override
    public SlaveController get() {
        // TODO: multi-tenant
        Machine m = provider.get();
        SshSlaveController sc = new SshSlaveController(m,keyPair);
        testCleaner.get().addTask(m);   // release a machine
        return sc;
    }
}
