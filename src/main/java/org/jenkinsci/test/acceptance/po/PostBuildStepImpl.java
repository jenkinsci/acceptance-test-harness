package org.jenkinsci.test.acceptance.po;

/**
 * {@link PageAreaImpl} that corresponds to 'Publisher' in the core.
 * <p/>
 * Subtypes should have {@link Describable} annotation on it.
 *
 * @author Kohsuke Kawaguchi
 */
public class PostBuildStepImpl extends AbstractStep implements PostBuildStep {
    public PostBuildStepImpl(Job parent, String path) {
        super(parent, path);
    }
}
