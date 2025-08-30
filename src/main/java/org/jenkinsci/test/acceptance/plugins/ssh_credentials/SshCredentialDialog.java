package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import org.jenkinsci.test.acceptance.plugins.credentials.BaseStandardCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.Credential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Modal dialog to enter a credential.
 *
 * @author Kohsuke Kawaguchi
 */
public class SshCredentialDialog extends BaseStandardCredentials {
    public final Control kind = control(by.xpath("//*[@id='credentials-dialog-form']//*[@path='/']"));

    public SshCredentialDialog(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Selects the credential type and bind the controls to the page area.
     */
    public <T extends Credential> T select(Class<T> type) {

        findCaption(type, new Resolver() {
            @Override
            protected void resolve(String caption) {
                kind.select(caption);
            }
        });

        return newInstance(type, getPage(), getPath());
    }
}
