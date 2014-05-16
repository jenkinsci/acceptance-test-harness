package org.jenkinsci.test.acceptance.plugins.script_security;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
public class PendingScript extends CapybaraPortingLayer {
    public final WebElement block;

    public PendingScript(Injector injector, WebElement block) {
        super(injector);
        this.block = block;
    }

    public void approve() {
        block.findElement(by.button("Approve"));
    }

    public void deny() {
        block.findElement(by.button("Deny"));
    }
}
