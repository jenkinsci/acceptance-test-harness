package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.jenkinsci.test.acceptance.resolver.JenkinsLinker;
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi-tenancy can be done as a filter.
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
@Singleton
public class MultitenancyMachineProvider implements MachineProvider {

    private final MachineProvider base;

    @Inject(optional = true)
    @Named("max_multitenant_machines")
    private final int max = 10;

    private final AtomicInteger cur = new AtomicInteger(1);


    private volatile Machine machine;

    @Inject
    public MultitenancyMachineProvider(MachineProvider base) {
        this.base = base;
        this.machine = base.get();
    }

    @Override
    public Machine get() {
        if (cur.incrementAndGet()==max) {
            synchronized (this){
                machine = base.get();
                cur.set(0);
            }

        }
        return new MultiTenantMachine(machine);
    }

    @Override
    public int[] getAvailableInboundPorts() {
        return base.getAvailableInboundPorts();
    }

    @Override
    public Authenticator authenticator() {
        return base.authenticator();
    }

    @Override
    public JenkinsResolver jenkinsResolver() {
        return base.jenkinsResolver();
    }
}
