package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page area base type for credentials
 * <p>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Credential extends PageAreaImpl {
    protected Credential(PageObject context, String path) {
        super(context, path);
    }

    protected Credential(PageArea area, String relativePath) {
        super(area, relativePath);
    }

    /**
     * Adds this credential and close the dialog.
     */
    public void add() {
        WebElement dialog = find(by.id("credentials-dialog-form"));
        WebElement we = find(submitButton());
        we.click();
        // wait for the form to be removed from the UI
        waitFor(driver).until(ExpectedConditions.invisibilityOf(dialog));
        // the notification bar can place itslef over other elements
        // so wait for it to be added and then disappear
        waitFor(waitFor(By.id("notification-bar"))).until(bar -> !bar.isDisplayed());
    }

    /**
     * @return dialog submit button selector
     */
    public static By submitButton() {
        return by.css(".jenkins-button[data-id='ok']");
    }
}
