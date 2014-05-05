package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page area base type for credentials
 * <p/>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Credential extends PageAreaImpl {
    protected Credential(PageObject context, String path) {
        super(context, path);
    }

    protected Credential(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }
}
