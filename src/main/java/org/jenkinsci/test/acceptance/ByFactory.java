package org.jenkinsci.test.acceptance;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
        try {
            XPathFactory.newInstance().newXPath().compile(xpath);
        } catch (XPathExpressionException ex) {
            throw new AssertionError("Invalid xpath syntax: " + xpath, ex);
        }
        return By.xpath(xpath);
    }

    public By xpath(String format, Object... args) {
        return xpath(String.format(format,args));
    }

    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     *
     * https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin
     */
    public By path(String path, Object... args) {
        return css("[path='%s']", String.format(path, args));
    }

    public By url(String path, Object... args) {
        return css("[url='%s']", String.format(path, args));
    }

    public By action(String path, Object... args) {
        return css("[action='%s']", String.format(path, args));
    }

    public By name(String name, Object... args) {
        return css("[name='%s']", String.format(name, args));
    }

    /**
     * Capybara's :link selector.
     *
     * @param locator
     *      Text, id, title, or image alt attribute of the link
     */
    public By link(String locator) {
        return xpath(".//A[@href][@id='%1$s' or normalize-space(.)='%1$s' or @title='%1$s' or .//img[@alt='%1$s']]",locator);
    }

    /**
     * Link href selector.
     *
     * @param locator
     *      href of the link
     */
    public By href(String locator){
        return css("a[href='%s']", locator);
    }

    /**
     * Finds checkbox.
     *
     * @param locator
     *      Text, id, title.
     */
    public By checkbox(String locator) {
        return xpath(fieldXPath("input[@type='checkbox']",locator));
    }

    /**
     * Select radio button by its name, id, or label text.
     */
    public By radioButton(String locator) {
        return xpath(fieldXPath("input[@type='radio']",locator));
    }

    /**
     * Finds input fields.
     *
     * @param locator
     *      Text, id, title.
     */
    public By input(String locator) {
        return xpath(fieldXPath("*[name()='INPUT' or name()='input' or name()='textarea' or name()='TEXTAREA' or name()='select' or name()='SELECT']",locator));
    }

    private static String fieldXPath(String base, String locator) {
        // TODO: there's actually a lot more
        return String.format(
                "  .//%2$s[./@id = '%1$s' or ./@name = '%1$s' or ./@value = '%1$s' or ./@placeholder = '%1$s' or ./@id = //label[contains(normalize-space(.), '%1$s')]/@for]"+
                "| .//label[contains(normalize-space(.), '%1$s')]//%2$s"+
                "| .//label[contains(normalize-space(.), '%1$s')][@class='attach-previous']/preceding-sibling::%2$s",locator, base);
    }

    /**
     * Finds a button
     */
    public By button(String locator) {
        return xpath(
                ".//input[./@type = 'submit' or ./@type = 'reset' or ./@type = 'image' or ./@type = 'button'][((./@id = '%1$s' or ./@name = '%1$s' or contains(./@value, '%1$s')) or contains(./@title, '%1$s'))] | .//input[./@type = 'image'][contains(./@alt, '%1$s')] | .//button[(((./@id = '%1$s' or contains(./@value, '%1$s')) or contains(normalize-space(.), '%1$s')) or contains(./@title, '%1$s'))] | .//input[./@type = 'image'][contains(./@alt, '%1$s')]"
                ,locator);
    }

    public By css(String css, Object... args) {
        return By.cssSelector(String.format(css, args));
    }

    public By tagName(String name) {
        return By.tagName(name);
    }

    public By option(String name) {
        return xpath(
            ".//option[contains(normalize-space(.), '%1$s') or @value='%1$s']", name
        );
    }

    public By id(String s) {
        return css("#"+s);
    }

    public By parent() {
        return xpath("..");
    }

    public By ancestor(String tagName) {
        return xpath("ancestor::%s[1]",tagName);
    }

    /**
     * "/foo/bar" matches div elements with path attribute "/foo/bar" or "/foo/bar[n]". Does not match "/foo/bar/baz" or "/foo/bar[1]/baz".
     */
    public By areaPath(final String pathPrefix) {
        final List<Character> delimiters = Arrays.asList('[', '/');
        final int prefixLength = pathPrefix.length();

        final By xpath = ByFactory.this.xpath("//div[starts-with(@path, '%s')]", pathPrefix);
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                ArrayList<WebElement> ret = new ArrayList<>();
                List<WebElement> allPrefixed = context.findElements(xpath);
                for (WebElement webElement: allPrefixed) {
                    String path = webElement.getAttribute("path");

                    // Ensure /foo matches /foo/bar and /boo[bar], but not /foolish/bartender
                    if (path.length() == prefixLength || delimiters.contains(path.charAt(prefixLength))) {
                        ret.add(webElement);
                    }
                }
                return ret;
            }

            @Override
            public String toString() {
                return "By page area name: " + pathPrefix;
            }
        };
    }
}
