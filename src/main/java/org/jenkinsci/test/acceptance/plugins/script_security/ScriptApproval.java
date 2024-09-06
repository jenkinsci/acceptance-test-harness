package org.jenkinsci.test.acceptance.plugins.script_security;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Administrator page to approve script security.
 *
 * @author Kohsuke Kawaguchi
 */
public class ScriptApproval extends PageObject {
    public ScriptApproval(Jenkins context) {
        super(context, context.url("scriptApproval/"));
    }

    /**
     * Finds a pending script that includes the given text in the context.
     */
    public PendingScript find(String context) {
        for (WebElement e : all(by.xpath(".//div[starts-with(@id,'ps-')]"))) {
            if (e.findElement(by.tagName("p")).getText().contains(context)) {
                return new PendingScript(injector, e);
            }
        }
        throw new NoSuchElementException(context);
    }

    /**
     * Finds a pending method signature that includes the given text in the signature itself.
     */
    public PendingSignature findSignature(String context) {
        for (WebElement e : all(by.xpath(".//div[starts-with(@id,'s-')]"))) {
            if (e.findElement(by.tagName("code")).getText().contains(context)) {
                return new PendingSignature(injector, e);
            }
        }
        throw new NoSuchElementException(context);
    }
}
