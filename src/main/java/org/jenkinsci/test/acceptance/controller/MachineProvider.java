package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Provider;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;

/**
 * Provider for {@link Machine}.
 *
 * {@link JenkinsProvider} and {@link SlaveProvider}s rely on {@link MachineProvider}
 * to acquire computers/VMs/containers to run Jenkins-under-test and slaves in.
 *
 * @author Vivek Pandey
 */
public interface MachineProvider extends Provider<Machine> {

    /**
     * TCP/IP ports on this machine  that can be used by the client of {@link MachineProvider}.
     */
    public int[] getAvailableInboundPorts();

    /**
     * A MachineProvider should encapsulates how authentication is done via
     * {@link Authenticator} contract.
     */
    public Authenticator authenticator();
}
