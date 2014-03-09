package org.jenkinsci.test.acceptance;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;

/**
 * More factories for {@link By} objects.
 *
 * <p>
 * To make the code flow, instantiate this object in the variable named "by"
 *
 * <p>
 * Mainly from Capybara's "selector.rb". To obtain the actual evaluation, I run
 * "bundle exec irb" from selenium-tests, then "require 'xpath'", and just evaluate
 * XPath::HTML.radio_button("XXX").
 *
 * @author Kohsuke Kawaguchi
 * @see PageObject#by
 */
public class ByFactory {
    public By xpath(String xpath) {
        return By.xpath(xpath);
    }

    public By xpath(String format, Object... args) {
        return By.xpath(String.format(format,args));
    }

    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     *
     * https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin
     */
    public By path(String path, Object... args) {
        return xpath(".//*[@path='%s']", String.format(path, args));
    }

    /**
     * Capybara's :link selector.
     *
     * @param locator
     *      Text, id, title, or image alt attribute of the link
     */
    public By link(String locator) {
        return xpath(".//A[@href][@id='%1$s' or text()='%1$s' or @title='%1$s' or .//img[@alt='%1$s']]",locator);
    }

    /**
     * Finds checkbox.
     *
     * @param locator
     *      Text, id, title.
     */
    public By checkbox(String locator) {
        return xpath(fieldXPath("//input[@type='checkbox']",locator));
    }

    /**
     * Finds input fields.
     *
     * @param locator
     *      Text, id, title.
     */
    public By input(String locator) {
        return xpath(fieldXPath("(//input|//textarea|//select)",locator));
    }

    private static String fieldXPath(String base, String locator) {
        // TODO: there's actually a lot more
        return String.format(base+"[@id='%1$s' or @attr='%1$s' or @name='%1$s' or @value='%1$s']",locator);
    }

    /**
     * Finds a button
     */
    public By button(String locator) {
        return xpath(
                ".//input[./@type = 'submit' or ./@type = 'reset' or ./@type = 'image' or ./@type = 'button'][((./@id = '%1$s' or contains(./@value, '%1$s')) or contains(./@title, '%1$s'))] | .//input[./@type = 'image'][contains(./@alt, '%1$s')] | .//button[(((./@id = '%1$s' or contains(./@value, '%1$s')) or contains(normalize-space(string(.)), '%1$s')) or contains(./@title, '%1$s'))] | .//input[./@type = 'image'][contains(./@alt, '%1$s')]"
                ,locator);
    }

    public By css(String css) {
        return By.cssSelector(css);
    }

    public By tagName(String name) {
        return By.tagName(name);
    }

    public By option(String name) {
        return xpath(
            ".//option[contains(normalize-space(string(.)), '%1$s') or @value='%1$s']", name
        );
    }
}
