package org.jenkinsci.test.acceptance.resolver;

import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.controller.Ssh;

import java.io.File;
import java.io.IOException;

/**
 * Uploads war from local to remote
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class JenkinsUploader implements JenkinsResolver {
    File war;

    public JenkinsUploader(File war) {
        this.war = war;
    }

    @Override
    public void materialize(Machine machine, String path) {
        try {
            Ssh ssh = machine.connect();

            // TODO: do it properly
            File target = new File(path);
            ssh.copyTo(war.getPath(), target.getName(), target.getParent());
        } catch (IOException e) {
            throw new AssertionError("Failed to copy "+war+" into "+path,e);
        }
    }
}
