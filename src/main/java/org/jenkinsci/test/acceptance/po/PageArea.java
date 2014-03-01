package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.cucumber.By2;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import static org.openqa.selenium.By.xpath;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class PageArea extends CapybaraPortingLayer {
    /**
     * Element path that points to this page area.
     */
    public final String path;

    protected PageArea(Injector injector, String path) {
        super(injector);
        this.path = path;
    }

    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     *
     * https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin
     */
    public By path(String rel) {
        return By2.path(path+'/'+rel);
    }

    /**
     * Find control on given path relative to the pagearea.
     * Several paths can be provided to find the first matching element. Useful
     * when element path changed between versions.
     */
    public WebElement control(String... relativePaths) {
        NoSuchElementException problem = new NoSuchElementException("No relative path specified!");
        for(String p : relativePaths) {
            try {
                return find(path(p));
            } catch (NoSuchElementException e) {
                problem = e;
            }
        }
        throw problem;
    }
}
