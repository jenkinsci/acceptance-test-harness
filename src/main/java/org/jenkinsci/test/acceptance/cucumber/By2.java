package org.jenkinsci.test.acceptance.cucumber;

import org.openqa.selenium.By;

/**
 * More factories for {@link By} objects.
 *
 * Mainly from Capybara's "selector.rb"
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class By2 extends org.openqa.selenium.By {
    public static By xpath(String xpath) {
        return By.xpath(xpath);
    }

    public static By xpath(String format, Object... args) {
        return By.xpath(String.format(format,args));
    }

    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     *
     * https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin
     */
    public static By path(String path) {
        return By2.xpath(".//*[@path='%s']",path);
    }

    /**
     * Capybara's :link selector.
     *
     * @param locator
     *      Text, id, title, or image alt attribute of the link
     */
    public static By link(String locator) {
        return xpath(".//A[@href]([@id='%1$s' | text()='%1$s' | @title='%1$s' | .//img[@alt='%1%s']]",locator);
    }
}
