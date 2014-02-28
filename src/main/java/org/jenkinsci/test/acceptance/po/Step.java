package org.jenkinsci.test.acceptance.po;

/**
 * Common part of {@link BuildStep} and {@link PostBuildStep}
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Step extends PageArea {
    protected Step(String path) {
        super(path);
    }
}
