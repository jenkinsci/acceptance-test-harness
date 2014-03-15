package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi-tenancy can be done as a filter.
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
@Singleton
public class MultitenancyMachineProvider implements MachineProvider{

    private final MachineProvider base;

    @Inject(optional = true)
    @Named("max_mt_machines")
    private int max = 10;

    private int cur = 0;


    private int currRawMachineCount=0;

    private final Map<String,AtomicInteger> machineReferences = new ConcurrentHashMap<>();
    private final Map<String,Machine> machineMap = new ConcurrentHashMap<>();

    private volatile Machine machine;

    @Inject
    public MultitenancyMachineProvider(@Named("raw") MachineProvider base) {
        logger.info("Initializing Mt Machine Provider...");
        this.base = base;
        this.machine = this.base.get();
        machineMap.put(machine.getId(),machine);
    }

    @Override
    public synchronized Machine get() {
        if (++cur==max) {

            boolean foundExisting = false;
            //Lets look for available machine, if other machine has available counts, lets use that
            for(String id : machineReferences.keySet()){
                if(machineReferences.get(id).get() < max){
                    if(machineMap.get(id) == null){
                        throw new IllegalStateException("No raw machine found in MT machine provider for id: "+id);
                    }
                    machine = machineMap.get(id);
                    foundExisting = true;
                    break;
                }
            }
            if(!foundExisting){
                logger.info(String.format("Max MT machine limit %s reached Getting new Machine instance...",max));
                machine = base.get();
                machineMap.put(machine.getId(),machine);
                cur=0;
            }
        }
        logger.info("Creating new MT machine...");
        MultiTenantMachine m = new MultiTenantMachine(this,machine);
        if(machineReferences.get(machine.getId()) != null){
            machineReferences.get(machine.getId()).incrementAndGet();
        }else{
            machineReferences.put(machine.getId(),new AtomicInteger(1));
        }
        return m;
    }

    @Override
    public int[] getAvailableInboundPorts() {
        return base.getAvailableInboundPorts();
    }

    @Override
    public Authenticator authenticator() {
        return base.authenticator();
    }


    public synchronized void offer(MultiTenantMachine m) throws IOException {
        logger.info(String.format("MT machine %s offered, will be recycled",m.getId()));
        AtomicInteger counter = machineReferences.get(m.baseMachine().getId());
        if(counter == null){
            throw new IllegalStateException(String.format("No raw machine found for MT machine: %s",m.getId()));
        }
        int remaining = counter.decrementAndGet();
        if(remaining == 0){
            m.baseMachine().close();
            machineReferences.remove(m.getId());
            machineMap.remove(m.getId());
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MultitenancyMachineProvider.class);
}
