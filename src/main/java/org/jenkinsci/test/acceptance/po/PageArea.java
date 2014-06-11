package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author christian.fritz
 */
public interface PageArea extends CapybaraPortingLayer, Control.Owner {
    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     * <p/>
     * https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin
     */
    @Override
    By path(String rel);

    /**
     * Create a control object that wraps access to the specific INPUT element in this page area.
     * <p/>
     * The {@link org.jenkinsci.test.acceptance.po.Control} object itself can be created early as the actual element
     * resolution happens lazily. This means {@link org.jenkinsci.test.acceptance.po.PageArea} implementations can put
     * these in their fields.
     * <p/>
     * Several paths can be provided to find the first matching element. Useful when element path changed between
     * versions.
     */
    Control control(String... relativePaths);

    Control control(By selector);

    /**
     * Returns {@link WebElement} that corresponds to the element that sits at the root
     * of the area this object represents.
     */
    WebElement self();

    String getPath();

    PageObject getPage();
}
