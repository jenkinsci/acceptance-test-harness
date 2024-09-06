package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Username with password")
public class UserPwdCredential extends BaseStandardCredentials {
    public final Control username = control(by.name("_.username"));
    public final Control password = control(by.name("_.password"));

    public UserPwdCredential(PageObject context, String path) {
        super(context, path);
    }

    public UserPwdCredential(PageArea area, String relativePath) {
        super(area, relativePath);
    }
}
