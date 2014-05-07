package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.junit.Resource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.security.acl.Owner;

/**
 * Wraps a specific form element in {@link PageArea} to provide operations.
 *
 * {@link Control} is like a {@link WebElement}, but with the following key differences:
 *
 * <ul>
 * <li>{@link Control} is late binding, and the underlying {@link WebElement} is resolved only when
 *     an interaction with control happens. This allows {@link Control}s to be instantiated earlier
 *     (typically when a {@link PageObject} subtype is instantiated.)
 * <li>{@link Control} offers richer methods to interact with a form element, making the right code easier to write.
 * </ul>
 *
 * See {@link PageArea} subtypes for typical usage.
 *
 * @author Kohsuke Kawaguchi
 * @see PageArea#control(String...)
 */
public class Control extends CapybaraPortingLayer {
    private final Owner parent;
    private final String[] relativePaths;

    public Control(PageArea parent, String... relativePaths) {
        super(parent.injector);
        this.parent = parent;
        this.relativePaths = relativePaths;
    }

    /**
     * Creates a control by giving their full path in the page
     */
    public Control(PageObject parent, String... paths) {
        super(parent.injector);
        this.parent = new Owner() {
            @Override
            public By path(String rel) {
                return by.path(rel);
            }
        };
        this.relativePaths = paths;
    }

    public Control(Injector injector, final By selector) {
        super(injector);
        this.relativePaths = new String[1];
        this.parent = new Owner() {
            @Override
            public By path(String rel) {
                return selector;
            }
        };
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

    public void set(Object text) {
        set(text.toString());
    }

    /**
     * Select an option.
     */
    public void select(String option) {
        WebElement e = resolve();
        e.findElement(by.option(option)).click();

        // move the focus away from the select control to fire onchange event
        e.sendKeys(Keys.TAB);
    }

    public void upload(Resource res) {
        resolve().sendKeys(res.asFile().getAbsolutePath());
    }

    public interface Owner {
        /**
         * Resolves relative path into a selector.
         */
        By path(String rel);
    }
}
