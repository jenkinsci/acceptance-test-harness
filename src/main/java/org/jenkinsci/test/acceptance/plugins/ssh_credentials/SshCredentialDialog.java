package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import org.jenkinsci.test.acceptance.plugins.credentials.Credential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Modal dialog to enter a credential.
 *
 * @author Kohsuke Kawaguchi
 */
public class SshCredentialDialog extends PageAreaImpl {
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

    /**
     * Adds this credential and close the modal dialog.
     */
    public void add() {
        final By addSubmitButton = by.id("credentials-add-submit-button");

        find(addSubmitButton).click();

        waitFor().withTimeout(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    return !find(addSubmitButton).isDisplayed();
                } catch (final NoSuchElementException | StaleElementReferenceException ex) {
                    return true;
                }
            }
        });
    }
}
