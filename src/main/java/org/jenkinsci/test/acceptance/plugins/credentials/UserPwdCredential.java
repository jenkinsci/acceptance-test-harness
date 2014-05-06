package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.plugins.credentials.Credential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Username with password")
public class UserPwdCredential extends Credential {
    public final Control username = control("username");
    public final Control description = control("description");
    public final Control password = control("password");

    public UserPwdCredential(PageObject context, String path) {
        super(context, path);
    }

    public UserPwdCredential(PageArea area, String relativePath) {
        super(area, relativePath);
    }

    /**
     * Adds this credential and close the dialog.
     */
    public void add() {
        find(by.id("credentials-add-submit-button")).click();
    }
}
