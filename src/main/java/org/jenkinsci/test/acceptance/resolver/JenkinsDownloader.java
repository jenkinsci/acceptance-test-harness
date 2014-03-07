package org.jenkinsci.test.acceptance.resolver;

import org.jenkinsci.test.acceptance.controller.Machine;

/**
 * TODO: download jenkins.war from URL
 *
 * @author Vivek Pandey
 */
public class JenkinsDownloader implements JenkinsResolver {
    @Override
    public void materialize(Machine machine, String path) {
        throw new UnsupportedOperationException();
    }
}
