package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class BuildWrapper extends PageArea {
    /**
     * @param path  p
     *      Each BuildWrapper occupies a unique path that is supplied by the subtype.
     */
    protected BuildWrapper(Job context, String path) {
        super(context, path);
    }

    /**
     * Checkbox that activates this build wrapper.
     */
    public final Control enable = control("");
}
