package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.plugins.credentials.Credential;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Modal dialog to enter a credential.
 *
 * @author Kohsuke Kawaguchi
 */
public class SshCredentialDialog extends PageArea {
    public final Control kind = control(by.xpath("//*[@id='credentials-dialog-form']//*[@path='/']"));

    public SshCredentialDialog(Injector injector, String path) {
        super(injector, path);
    }

    public SshCredentialDialog(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Selects the credential type and bind the controls to the page area.
     */
    public <T extends Credential> T select(Class<T> type) {
        String sut_type = type.getAnnotation(CredentialPageObject.class).value();

        kind.select(sut_type);

        return newInstance(type, injector, path);
    }

    /**
     * Adds this credential and close the modal dialog.
     */
    public void add() {
        find(by.id("credentials-add-submit-button")).click();
    }
}
