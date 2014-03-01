package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildStep extends Step {
    public BuildStep(Job parent, String path) {
        super(parent,path);
    }
}
