package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

/**
 * Modal dialog to enter a credential.
 *
 * @author Kohsuke Kawaguchi
 */
public class SshCredentialDialog extends PageArea {
    public final Control kind = new Control(injector,by.xpath("//*[@id='credentials-dialog-form']//*[@path='/']"));

    public final Control username = control("username");
    public final Control description = control("description");

    public SshCredentialDialog(Injector injector, String path) {
        super(injector, path);
    }

    public SshCredentialDialog(PageObject context, String path) {
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
