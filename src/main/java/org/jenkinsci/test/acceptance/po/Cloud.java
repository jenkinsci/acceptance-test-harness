package org.jenkinsci.test.acceptance.po;

/**
 * Configuration fragment for cloud.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Cloud extends PageAreaImpl {
    protected Cloud(PageObject context, String path) {
        super(context, path);
    }
}
