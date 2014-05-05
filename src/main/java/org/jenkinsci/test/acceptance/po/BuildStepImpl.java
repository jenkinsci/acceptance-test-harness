package org.jenkinsci.test.acceptance.po;

/**
 * Job build step.
 * <p/>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildStepImpl extends AbstractStep implements BuildStep {
    public BuildStepImpl(Job parent, String path) {
        super(parent, path);
    }
}
