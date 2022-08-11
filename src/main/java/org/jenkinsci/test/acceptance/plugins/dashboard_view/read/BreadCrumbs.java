package org.jenkinsci.test.acceptance.plugins.dashboard_view.read;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a simple area for reading the breadcrumbs on the page.
 *
 * @author Peter Müller
 */
public class BreadCrumbs extends PageAreaImpl {

    /**
     * The path to the breadcrumbs list.
     */
    final By nameCrumb = By.xpath("//ol[@id=\"breadcrumbs\"] | //ul[@id=\"breadcrumbs\"]");

    /**
     * Create a new object for reading the breadcrumbs in the dashboard view.
     *
     * @param context
     * @param path
     */
    public BreadCrumbs(PageObject context, String path) {
        super(context, path);
    }


    /**
     * Get the text of the breadcrumbs from left to right.
     *
     * @return List of text in each breadcrumb.
     */
    public List<String> getBreadCrumbs() {
        final WebElement webElement = find(nameCrumb);
        final String[] crumbs = webElement.getText().split("\n");
        return Arrays.asList(crumbs);
    }
}
