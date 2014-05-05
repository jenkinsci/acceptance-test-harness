package org.jenkinsci.test.acceptance.po;

/**
 * Common part of {@link BuildStep} and {@link PostBuildStep}
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractStep extends PageAreaImpl implements Step {
    public final Job parent;

    public AbstractStep(Job parent, String path) {
        super(parent, path);
        this.parent = parent;
    }
}
