package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Wraps a specific control in {@link PageArea} to provide operations.
 * This makes it easier to write {@link PageArea}s.
 *
 * @author Kohsuke Kawaguchi
 * @see PageArea#control(String...)
 */
public class Control extends CapybaraPortingLayer {
    private final PageArea parent;
    private final String[] relativePaths;

    public Control(PageArea parent, String[] relativePaths) {
        super(parent.injector);
        this.parent = parent;
        this.relativePaths = relativePaths;
    }

    public WebElement resolve() {
        NoSuchElementException problem = new NoSuchElementException("No relative path specified!");
        for(String p : relativePaths) {
            try {
                return find(parent.path(p));
            } catch (NoSuchElementException e) {
                problem = e;
            }
        }
        throw problem;
    }

    public void sendKeys(String t) {
        resolve().sendKeys(t);
    }

    public void uncheck() {
        check(resolve(), false);
    }

    public void check() {
        check(resolve(),true);
    }

    public void check(boolean state) {
        check(resolve(),state);
    }

    public void click() {
        resolve().click();
    }

    public void set(String text) {
        WebElement e = resolve();
        e.clear();
        e.sendKeys(text);
    }
}
