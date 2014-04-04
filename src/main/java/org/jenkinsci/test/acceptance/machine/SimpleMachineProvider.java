package org.jenkinsci.test.acceptance.machine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jenkinsci.test.acceptance.Authenticator;
import org.jenkinsci.test.acceptance.PublicKeyAuthenticator;
import org.jenkinsci.test.acceptance.SshKeyPair;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Stephen Connolly
 */
public class SimpleMachineProvider implements MachineProvider {

    private Authenticator authenticator;

    private String ipAddress;

    private String user;

    private SshKeyPair keyPair;

    private int[] inboundPorts;

    private final AtomicInteger nextId;

    @Inject
    public SimpleMachineProvider(@Named("firstPort") int firstInboundPort,
                                 @Named("lastPort") int lastInboundPort,
                                 @Named("host") String ipAddress,
                                 @Named("user") String user,
                                 SshKeyPair keyPair) {
        int first = Math.min(65535, Math.max(1024, Math.min(firstInboundPort, lastInboundPort)));
        int last = Math.min(65535, Math.max(1024, Math.max(firstInboundPort, lastInboundPort)));
        inboundPorts = new int[Math.max(1, last - first + 1)];
        for (int port = first, i = 0; port <= last; port++, i++) {
            inboundPorts[i] = port;
        }
        nextId = new AtomicInteger(1);
        this.ipAddress = ipAddress;
        this.user = user;
        this.keyPair = keyPair;
        this.authenticator = new PublicKeyAuthenticator(user, keyPair);
    }

    @Override
    public int[] getAvailableInboundPorts() {
        return inboundPorts.clone();
    }

    @Override
    public Authenticator authenticator() {
        return authenticator;
    }

    @Override
    public Machine get() {
        return new SimpleMachine(this, Integer.toString(nextId.getAndIncrement()), ipAddress, user);
    }
}
