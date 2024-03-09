package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import java.time.Duration;
import org.jenkinsci.test.acceptance.plugins.credentials.Credential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

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
        this.findAndPerformClick(Credential.submitButton());

        waitFor().withTimeout(Duration.ofSeconds(5)).until(() -> {
            try {
                return !find(Credential.submitButton()).isDisplayed();
            } catch (final NoSuchElementException | StaleElementReferenceException ex) {
                return true;
            }
        });
    }

    private void findAndPerformClick(final By addSubmitButtonLocator) {
        final WebElement addSubmitButton = find(addSubmitButtonLocator);

        final Actions builder = new Actions(driver);

        new Scroller().scrollIntoView(addSubmitButton, driver);
        addSubmitButton.click();
        builder.perform();
    }
}
