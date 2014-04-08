package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.plugins.credentials.Credential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("SSH Username with private key")
public class SshPrivateKeyCredential extends Credential {
    public final Control username = control("username");
    public final Control description = control("description");

    public SshPrivateKeyCredential(Injector injector, String path) {
        super(injector, path);
    }

    public SshPrivateKeyCredential(PageObject context, String path) {
        super(context, path);
    }

    public Direct selectEnterDirectly() {
        WebElement e = choose("Enter directly");
        return new Direct(injector, e.getAttribute("path"));
    }

    public static class Direct extends PageArea {
        public final Control privateKey = control("privateKey");

        public Direct(Injector injector, String path) {
            super(injector, path);
        }
    }

    /**
     * Adds this credential and close the dialog.
     */
    public void add() {
        find(by.id("credentials-add-submit-button")).click();
    }
}
