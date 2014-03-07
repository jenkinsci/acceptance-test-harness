package org.jenkinsci.test.acceptance.controller;

import org.jclouds.compute.domain.NodeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Vivek Pandey
 */
public class JcloudsMachine implements Machine {
    private final NodeMetadata nodeMetadata;
    private final MachineProvider machineProvider;

    public static final int BEGINNING_PORT = 20000;

    private AtomicInteger nextPort = new AtomicInteger(0);
    private List<Integer> availablePorts;
    private final int maxAvailablePort;

    public JcloudsMachine(MachineProvider machineProvider, NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
        this.machineProvider = machineProvider;
        List<Integer> ports = new ArrayList<>();
        for(int port:machineProvider.getAvailableInboundPorts()){
            ports.add(port);
        }

        this.availablePorts = Collections.unmodifiableList(ports);
        this.maxAvailablePort = ports.get(ports.size()-1);

        //set to the first port
        nextPort.set(availablePorts.get(0));

    }

    @Override
    public String getPublicIpAddress(){
        return nodeMetadata.getPublicAddresses().iterator().next();
    }

    @Override
    public String getUser(){
        return (nodeMetadata.getCredentials() == null) ? "ubuntu" : nodeMetadata.getCredentials().getUser();
    }

    /**
     * Terminate a running machine
     */
    @Override
    public void terminate(){
        logger.error("Destroying node: "+nodeMetadata);
        machineProvider.destroy(nodeMetadata.getId());
    }

    @Override
    public int getNextAvailablePort(){
        int p = nextPort.incrementAndGet();
        if(p > maxAvailablePort){
            throw new RuntimeException("no more available ports");
        }
        return p;
    }

    /**
     *  Terminate all running instances in the group this machine instance is created in to
     */
//    public void terminateAll(){
//        logger.info("Destroying nodes in group %s%n", nodeMetadata.getGroup());
//
//        // you can use predicates to select which nodes you wish to destroy.
//        Set<? extends NodeMetadata> destroyed = computeService.destroyNodesMatching(Predicates.and(not(TERMINATED), inGroup(nodeMetadata.getGroup())));
//        System.out.printf("Destroyed nodes %s%n", destroyed);
//    }





    private static final Logger logger = LoggerFactory.getLogger(JcloudsMachine.class);


}
