package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author christian.fritz
 */
public interface PageArea extends CapybaraPortingLayer, Control.Owner {
    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     * <p>
     * <a href="https://plugins.jenkins.io/form-element-path/">form-element-path-plugin</a>
     */
    @Override
    By path(String rel);

    /**
     * Create a control object that wraps access to the specific INPUT element in this page area.
     * <p>
     * The {@link org.jenkinsci.test.acceptance.po.Control} object itself can be created early as the actual element
     * resolution happens lazily. This means {@link org.jenkinsci.test.acceptance.po.PageArea} implementations can put
     * these in their fields.
     * <p>
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
    String getPath(String rel);
    String getPath(String rel, int index);

    PageObject getPage();

    /**
     * Capture path attribute of newly created form chunk upon invoking action.
     * <p>
     * Consider "Add" button in page area with path "/foo" that is supposed to create new page area with path "/foo/bar"
     * or "/foo/bar[n]". There are several problems with the straightforward approach:
     *  - Created area may or may not be the first one of its kind so figuring the "path" is nontrivial.
     *  - The area may can take a while to render so waiting is needed.
     *  - Even after the markup appears, it can take a while for "path" attribute is added.
     * <p>
     * This method properly wait until the new path is known. To be used as:
     *
     * <pre>
     * {@code
     *  String barPath = fooArea.createPageArea("bar", () -> control("add-button").click());
     *  new FooBarArea(fooArea, barPath);
     *  }
     *  </pre>
     *
     * @param name Name of the surrounding div which will the next segment in path. Given the area is "/builder" and we are
     *             about to construct /builder/shell[3], the relative prefix is "shell".
     * @param action An action that triggers the page area creation. Clicking the button, etc.
     * @return The surrounding path of the area, exception thrown when not able to find out.
     */
    @NonNull String createPageArea(String name, Runnable action) throws TimeoutException;
}
