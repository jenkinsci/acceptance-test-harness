package org.jenkinsci.test.acceptance.po;

/**
 * Job build step.
 *
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildStep extends Step {
    public BuildStep(Job parent, String path) {
        super(parent,path);
    }
}
