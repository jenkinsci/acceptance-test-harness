package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import org.jenkinsci.test.acceptance.plugins.credentials.Credential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("SSH Username with private key")
public class SshPrivateKeyCredential extends Credential {
    public final Control username = control("username");
    public final Control description = control("description");

    public SshPrivateKeyCredential(PageObject context, String path) {
        super(context, path);
    }

    public SshPrivateKeyCredential(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }

    public Direct selectEnterDirectly() {
        WebElement e = choose("Enter directly");
        return new Direct(getPage(), e.getAttribute("path"));
    }

    public static class Direct extends PageAreaImpl {
        public final Control privateKey = control("privateKey");

        public Direct(PageObject parent, String path) {
            super(parent, path);
        }
    }

    /**
     * Adds this credential and close the dialog.
     */
    public void add() {
        find(by.id("credentials-add-submit-button")).click();
    }
}
