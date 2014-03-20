package org.jenkinsci.test.acceptance.machines;

import org.jclouds.compute.domain.NodeMetadata;
import org.jenkinsci.test.acceptance.Ssh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
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
    private final JcloudsMachineProvider machineProvider;

    public static final int BEGINNING_PORT = 20000;

    private final Stack<Integer> availablePorts = new Stack<>();

    private final String dir;

    public JcloudsMachine(JcloudsMachineProvider machineProvider, NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
        this.machineProvider = machineProvider;
        for(int port:machineProvider.getAvailableInboundPorts()){
            availablePorts.push(port);
        }

        Ssh ssh = connect();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ssh.executeRemoteCommand("echo `pwd`/machine_home_"+newDirSuffix()+"/",baos);

        this.dir = new String(baos.toByteArray()).trim();

        ssh.executeRemoteCommand("mkdir -p "+this.dir);
    }

    @Override
    public String getId() {
        return nodeMetadata.getId();
    }

    @Override
    public Ssh connect() {
        try {
            Ssh ssh = new Ssh(getPublicIpAddress());
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
        logger.info("Destroying node: " + nodeMetadata);
        Ssh ssh = connect();
        try {
            ssh.getConnection().exec(String.format("pkill -u $(id -u %s)", getUser()), System.out);
        } catch (InterruptedException e) {
            //ignore
            logger.error(e.getMessage());
        }
        machineProvider.offer(this);
    }

    public static long newDirSuffix(){
        SecureRandom secureRandom = new SecureRandom();
        long secureInitializer = secureRandom.nextLong();
        return Math.abs(new Random( secureInitializer + Runtime.getRuntime().freeMemory()).nextInt());
    }

    private static final Logger logger = LoggerFactory.getLogger(JcloudsMachine.class);


}
