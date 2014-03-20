package org.jenkinsci.test.acceptance.resolver;

import org.jenkinsci.test.acceptance.machines.Machine;
import org.jenkinsci.test.acceptance.Ssh;

/**
 * Creates a soft link on the file system. To be used only with {@link org.jenkinsci.test.acceptance.machines.MultitenancyMachineProvider}
 *
 * @author Vivek Pandey
 */
public class JenkinsLinker implements JenkinsResolver{

    private String jenkinsWarLocation;

    public JenkinsLinker(String jenkinsWarLocation) {
        this.jenkinsWarLocation = jenkinsWarLocation;
    }

    @Override
    public void materialize(Machine machine, String path) {
        Ssh ssh = machine.connect();
        ssh.executeRemoteCommand(String.format("ln -s `pwd`/%s %s", jenkinsWarLocation, path));
    }
}
