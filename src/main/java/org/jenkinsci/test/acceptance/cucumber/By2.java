package org.jenkinsci.test.acceptance.cucumber;

import org.openqa.selenium.By;

/**
 * More factories for {@link org.openqa.selenium.By} objects.
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
    public static By path(String path, Object... args) {
        return By2.xpath(".//*[@path='%s']",String.format(path,args));
    }

    /**
     * Capybara's :link selector.
     *
     * @param locator
     *      Text, id, title, or image alt attribute of the link
     */
    public static By link(String locator) {
        return xpath(".//A[@href][@id='%1$s' or text()='%1$s' or @title='%1$s' or .//img[@alt='%1$s']]",locator);
    }

    /**
     * Finds checkbox.
     *
     * @param locator
     *      Text, id, title.
     */
    public static By checkbox(String locator) {
        return xpath("//input[@type='checkbox']"+fieldXPath(locator));
    }

    private static String fieldXPath(String locator) {
        // TODO: there's actually a lot more
        return String.format("[@id='%1$s' or @attr='%1$s' or @name='%1$s']",locator);
    }

    /**
     * Finds a button
     */
    public static By button(String locator) {
        return xpath(
                "//input[@type='submit' or @type='reset' or @type='image' or @type='button'][@id='%1$s' or @value='%1$s' or @title='%1$s'] |"+
                "//button[@id='%1$s' or text()='%1$s' or @value='%1$s' or @title='%1$s']"
                ,locator);
    }
}
