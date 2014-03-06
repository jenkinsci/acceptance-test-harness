package org.jenkinsci.test.acceptance.controller;

import org.jclouds.compute.domain.NodeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vivek Pandey
 */
public class Machine {
    private final NodeMetadata nodeMetadata;
    private final MachineProvider machineProvider;

    public Machine(MachineProvider machineProvider, NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
        this.machineProvider = machineProvider;
    }

    public NodeMetadata getNodeMetadata() {
        return nodeMetadata;
    }

    /**
     * Terminate a running machine
     */
    public void terminate(){
        logger.error("Destroying node: "+nodeMetadata);
        machineProvider.destroy(nodeMetadata.getId());
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





    private static final Logger logger = LoggerFactory.getLogger(Machine.class);
}
