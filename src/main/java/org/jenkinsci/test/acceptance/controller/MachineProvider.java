package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Provider;
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver;

/**
 * Provider for {@link org.jenkinsci.test.acceptance.controller.Machine}
 *
 * @author Vivek Pandey
 */
public interface MachineProvider extends Provider<Machine>{

    public int[] getAvailableInboundPorts();

    /**
     * A MachineProvider should encapsulates how authentication is done via
     * {@link org.jenkinsci.test.acceptance.controller.Authenticator} contract.
     */
    public Authenticator authenticator();
}
