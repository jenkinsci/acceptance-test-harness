package org.jenkinsci.test.acceptance.resolver;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.controller.Ssh;

import java.io.IOException;

/**
 * TODO: download jenkins.war from URL
 *
 * @author Vivek Pandey
 */
public class JenkinsDownloader implements JenkinsResolver {

    private final String jenkinsWarLocation;

    @Inject
    public JenkinsDownloader(@Named("jenkins-war-location")String jenkinsWarLocation) {
        this.jenkinsWarLocation = jenkinsWarLocation;
    }

    @Override
    public void materialize(Machine machine, String path) {
        Ssh ssh = machine.connect();
        ssh.executeRemoteCommand(String.format("wget -O %s %s",path, jenkinsWarLocation));
    }
}
