package org.jenkinsci.test.acceptance.po;

/**
 * {@link PageArea} that corresponds to 'Publisher' in the core.
 *
 * Subtypes should have {@link BuildStepPageObject} annotation on it.
 *
 * @author Kohsuke Kawaguchi
 */
public class PostBuildStep extends Step {
    public PostBuildStep(Job parent, String path) {
        super(parent, path);
    }
}
