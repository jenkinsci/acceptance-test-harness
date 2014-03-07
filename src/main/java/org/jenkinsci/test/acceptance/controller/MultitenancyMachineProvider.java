package org.jenkinsci.test.acceptance.controller;

/**
 * Multi-tenancy can be done as a filter.
 *
 * @author Kohsuke Kawaguchi
 */
public class MultitenancyMachineProvider implements MachineProvider {
    MachineProvider base;
    final int max;
    int cur;

    private Machine machine;

    public MultitenancyMachineProvider(MachineProvider base, int max) {
        this.base = base;
        this.max = max;
    }

    @Override
    public Machine get() {
        if (cur++==max) {
            machine = base.get();
            cur=0;
        }

        // TODO: curve up a section from #machine by creating new user, etc
        Machine child = ...;

        return child;
    }
}
