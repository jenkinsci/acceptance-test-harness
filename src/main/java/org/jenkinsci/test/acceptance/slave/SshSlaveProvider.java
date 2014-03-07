package org.jenkinsci.test.acceptance.slave;

import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.controller.MachineProvider;
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
    MachineProvider provider;

    @Inject
    Provider<TestCleaner> testCleaner;

    @Override
    public SlaveController get() {
        // TODO: multi-tenant
        Machine m = provider.get();
        SshSlaveController sc = new SshSlaveController(m);
        testCleaner.get().addTask(m);   // release a machine
        return sc;
    }
}
