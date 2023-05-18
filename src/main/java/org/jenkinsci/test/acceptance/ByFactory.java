package org.jenkinsci.test.acceptance;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Construct XPath inserting placeholders using String.format.
     *
     * @param format Format specifier for xpath. Only string placeholders are supported - do not put quotes around them.
     */
    public By xpath(String format, Object... args) {
        return xpath(formatXPath(format, args));
    }

    // XPath 1.0 supported by JDK and browsers has no way to escape quotes in string literals. Therefore whenever such
    // argument is specified it needs to be delimited by the other kind of quotes. In case the string contains both kind
    // of quotes, it needs to be split to substrings containing only one kind of quotes each so the literals can be quoted
    // as described earlier and then glued together using xpath's concat function. Note that we can not use variable resolvers
    // or XPath 2.0 here as java is not executing the xpath, it merely passes that to browser to execute. To make this a
    // bit more fun, this is an API relied upon external clients that might have written something like `xpath("//foo[text()='%s']", var)`
    // so we need to control the quotes around String.format placeholders as well.
    // Therefore we ...
    /*package for testing*/ String formatXPath(String format, Object... args) {
        String[] placeholders = new String[args.length];
        String[] sanitized = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            placeholders[i] = "placeholder" + (i + 1);
            // Sanitize xpath arguments appropriately
            sanitized[i] = xq(String.valueOf(args[i]));
        }
        // Fill the pattern with unique placeholders so we can safely identify what appears where so we do not
        // have to support various String.format specifiers.
        String marker = String.format(format, (Object[]) placeholders);
        // Then replace the placeholders with quotes around them with unquoted format sequences
        String unquotedFormat = marker.replaceAll("(['\" ])placeholder(\\d+)\\1", "%$2\\$s");

        // Format the template with quoted arguments
        String quotedSanitizedFormat = String.format(unquotedFormat, (Object[]) sanitized);

        // Placeholders that are part of longer string literal will not be escaped
        String finalFormat = quotedSanitizedFormat.replaceAll("placeholder(\\d+)", "%$1\\$s");
        return String.format(finalFormat, args);
    }

    private String xq(String value) {
        boolean quote = value.contains("'");
        boolean doublequote = value.contains("\"");
        if (quote && doublequote) {
            return "concat('" + value.replace("'", "', \"'\", '") + "', '')";
        } else if (quote){
            return '"' + value + '"';
        } else {
            return "'" + value + "'";
        }
    }

    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     * <p>
     * <a href="https://plugins.jenkins.io/form-element-path/">form-element-path-plugin</a>
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
     *      Text
     */
    public By checkbox(String locator) {
        return xpath(String.format(".//label[contains(normalize-space(.), '%1$s')]", locator));
    }

    /**
     * Select radio button by its label text.
     */
    public By radioButton(String locator) {
        return xpath(String.format(".//label[contains(normalize-space(.), '%1$s')]", locator));
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
                "| .//label[contains(normalize-space(.), '%1$s')][contains(@class, 'attach-previous')]/preceding-sibling::%2$s",locator, base);
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
            ".//option[normalize-space(.)='%1$s' or @value='%1$s']", name
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
        final int prefixLength = pathPrefix.length();

        final By xpath = ByFactory.this.xpath("//div[starts-with(@path, '%s')]", pathPrefix);
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                ArrayList<WebElement> ret = new ArrayList<>();
                List<WebElement> allPrefixed = context.findElements(xpath);
                for (WebElement webElement: allPrefixed) {
                    String path = webElement.getAttribute("path");

                    // Ensure /foo matches /foo and /boo[bar], but not /foo/bar or /foolish/bartender
                    if (path.substring(prefixLength).matches("^(\\[[^\\]]+\\]|)$")) {
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

    /**
     * @param linkText The text to match against
     * @return a By which locates A elements that contain the given link text
     */
    public By partialLinkText(final String linkText) {
        return By.partialLinkText(linkText);
    }

}
