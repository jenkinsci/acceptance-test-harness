package org.jenkinsci.test.acceptance.controller;

import org.jclouds.compute.domain.NodeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.EmptyStackException;
import java.util.Random;
import java.util.Stack;

/**
 * @author Vivek Pandey
 */
public class JcloudsMachine implements Machine {
    private final NodeMetadata nodeMetadata;
    private final JCloudsMachineProvider machineProvider;

    public static final int BEGINNING_PORT = 20000;

    private final Stack<Integer> availablePorts = new Stack<>();

    private final String dir;
    private final String jenkinsHome;

    public JcloudsMachine(JCloudsMachineProvider machineProvider, NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
        this.machineProvider = machineProvider;
        for(int port:machineProvider.getAvailableInboundPorts()){
            availablePorts.push(port);
        }

        this.dir = "./machine_home_"+newDirSuffix();
        Ssh ssh = connect();
        ssh.executeRemoteCommand("mkdir -p "+this.dir);

        this.jenkinsHome = this.dir+"/jenkins.war";

        //install jenkins
        machineProvider.jenkinsResolver().materialize(this,jenkinsHome);

    }

    @Override
    public Ssh connect() {
        try {
            Ssh ssh = new Ssh(getUser(),getPublicIpAddress());
            machineProvider.authenticator().authenticate(ssh.getConnection());
            return ssh;
        } catch (IOException e) {
            throw new AssertionError("Failed to create ssh connection",e);
        }
    }

    @Override
    public String getPublicIpAddress(){
        return nodeMetadata.getPublicAddresses().iterator().next();
    }

    @Override
    public String getUser(){
        return (nodeMetadata.getCredentials() == null) ? "ubuntu" : nodeMetadata.getCredentials().getUser();
    }

    @Override
    public String dir() {
        return dir;
    }

    @Override
    public int getNextAvailablePort(){
        try{
            return availablePorts.pop();
        }catch (EmptyStackException e){
            throw new AssertionError("No more free inbound ports",e);
        }
    }

    @Override
    public void close() throws IOException {
        logger.error("Destroying node: "+nodeMetadata);
        machineProvider.destroy(nodeMetadata.getId());
    }

    @Override
    public String jenkinsWarLocation() {
        return jenkinsHome;
    }

    public static long newDirSuffix(){
        SecureRandom secureRandom = new SecureRandom();
        long secureInitializer = secureRandom.nextLong();
        return Math.abs(new Random( secureInitializer + Runtime.getRuntime().freeMemory()).nextInt());
    }

    private static final Logger logger = LoggerFactory.getLogger(JcloudsMachine.class);


}
