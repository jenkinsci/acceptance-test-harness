package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
public class PostBuildStep extends Step {
    public PostBuildStep(Job parent, String path) {
        super(parent, path);
    }
}
