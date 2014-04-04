package org.jenkinsci.test.acceptance.machine;

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
 * @author Stephen Connolly
 */
public class SimpleMachine implements Machine {

    private final SimpleMachineProvider machineProvider;

    private final String id;

    private final String ipAddress;

    private final String user;
    private final String dir;

    private final Stack<Integer> availablePorts = new Stack<>();


    public SimpleMachine(SimpleMachineProvider machineProvider, String id, String ipAddress, String user) {
        this.machineProvider = machineProvider;
        this.id = id;
        this.ipAddress = ipAddress;
        for (int port : machineProvider.getAvailableInboundPorts()) {
            availablePorts.push(port);
        }
        this.user = user;

        try (Ssh ssh = connect()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ssh.executeRemoteCommand("echo `pwd`/machine_home_" + newDirSuffix() + "/", baos);

            this.dir = new String(baos.toByteArray()).trim();

            ssh.executeRemoteCommand("mkdir -p " + this.dir);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Ssh connect() {
        Ssh ssh = null;
        try {
            ssh = new Ssh(getPublicIpAddress());
            machineProvider.authenticator().authenticate(ssh.getConnection());
            return ssh;
        } catch (IOException e) {
            if (ssh != null) {
                ssh.close();
            }
            throw new AssertionError("Failed to create ssh connection",e);
        }
    }

    @Override
    public String getPublicIpAddress() {
        return ipAddress;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String dir() {
        return dir;
    }

    @Override
    public int getNextAvailablePort() {
        try {
            return availablePorts.pop();
        } catch (EmptyStackException e) {
            throw new AssertionError("No more free inbound ports", e);
        }
    }

    @Override
    public void close() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static long newDirSuffix() {
        SecureRandom secureRandom = new SecureRandom();
        long secureInitializer = secureRandom.nextLong();
        return Math.abs(new Random(secureInitializer + Runtime.getRuntime().freeMemory()).nextInt());
    }

    private static final Logger logger = LoggerFactory.getLogger(SimpleMachine.class);
}
