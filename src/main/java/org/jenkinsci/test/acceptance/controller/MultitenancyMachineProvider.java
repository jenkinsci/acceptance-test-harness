package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Named("max_mt_machines")
    private int max = 10;

    private final AtomicInteger cur = new AtomicInteger(0);


    private volatile Machine machine;

    @Inject
    public MultitenancyMachineProvider(@Named("raw") MachineProvider base) {
        logger.info("Initializing Mt Machine Provider...");
        this.base = base;
        this.machine = this.base.get();
    }

    @Override
    public Machine get() {
        if (cur.incrementAndGet()==max) {
            synchronized (this){
                logger.info(String.format("Max MT machine limit %s reached Getting new Machine instance...",max));
                machine = base.get();
                cur.set(0);
            }

        }
        logger.info("Creating new MT machine...");
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

    private static final Logger logger = LoggerFactory.getLogger(MultitenancyMachineProvider.class);

}
