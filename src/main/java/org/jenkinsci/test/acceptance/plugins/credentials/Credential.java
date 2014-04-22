package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page area base type for credentials
 *
 * @author Kohsuke Kawaguchi
 * @see Describable
 */
public abstract class Credential extends PageArea {
    protected Credential(PageObject context, String path) {
        super(context, path);
    }

    protected Credential(PageArea area, String relativePath) {
        super(area, relativePath);
    }
}
