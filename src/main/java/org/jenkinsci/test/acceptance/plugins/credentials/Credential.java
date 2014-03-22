package org.jenkinsci.test.acceptance.plugins.credentials;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page area base type for credentials
 *
 * @author Kohsuke Kawaguchi
 * @see CredentialPageObject
 */
public abstract class Credential extends PageArea {
    protected Credential(Injector injector, String path) {
        super(injector, path);
    }

    protected Credential(PageObject context, String path) {
        super(context, path);
    }
}
