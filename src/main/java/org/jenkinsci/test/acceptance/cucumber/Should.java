package org.jenkinsci.test.acceptance.cucumber;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jenkinsci.test.acceptance.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Used to bind "should" or "should not" keyword in Gherkin into an object.
 *
 * <p>
 * We cannot use enum here because of the inflexible argument type conversion in cucumber.
 *
 * @author Kohsuke Kawaguchi
 */
public class Should {
    /**
     * True if this object represents "should", false if "should not"
     */
    public final boolean value;

    public Should(String token) {
        if (token.equals("should"))
            value = true;
        else
        if (token.equals("should not"))
            value = false;
        else
        throw new AssertionError("Unexpected: "+token);
    }

    /**
     * Creates a matcher that asserts the presence (or absence) of the element.
     */
    public Matcher<WebDriver> haveElement(By path) {
        return apply(Matchers.hasElementOf(path));
    }

    /**
     * Creates a matcher that asserts the presence (or absence) of the content.
     */
    public Matcher<WebDriver> haveContent(String content) {
        return apply(Matchers.hasContent(content));
    }

    public Matcher<WebDriver> apply(Matcher<WebDriver> m) {
        if (!value)  m = CoreMatchers.not(m);
        return m;
    }
}
