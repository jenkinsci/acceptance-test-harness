package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildStep extends Step {
    public final Job parent;

    public BuildStep(Job parent, String path) {
        super(path);
        this.parent = parent;
    }
}
