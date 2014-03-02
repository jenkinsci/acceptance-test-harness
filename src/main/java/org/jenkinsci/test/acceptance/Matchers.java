package org.jenkinsci.test.acceptance;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jenkinsci.test.acceptance.steps.JobSteps;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.CoreMatchers.*;

/**
 * Hamcrest matchers.
 *
 * @author Kohsuke Kawaguchi
 */
public class Matchers {
    /**
     * Asserts that {@link WebDriver#getPageSource()} contains the given string.
     */
    public static Matcher<WebDriver> hasContent(final String content) {
      return new TypeSafeMatcher<WebDriver>() {
          @Override
          protected boolean matchesSafely(WebDriver item) {
              return item.getPageSource().contains(content);
          }

          @Override
          public void describeTo(Description description) {
              description.appendText("Text containing "+content);
          }

          @Override
          protected void describeMismatchSafely(WebDriver item, Description mismatchDescription) {
              mismatchDescription.appendText("was ")
                      .appendValue(item.getCurrentUrl())
                      .appendText("\n")
                      .appendValue(item.getPageSource());
          }
      };
    }

    /**
     * Matches that matches {@link WebDriver} when it has an element that matches to the given selector.
     */
    public static Matcher<WebDriver> hasElement(final By selector) {
        return new TypeSafeMatcher<WebDriver>() {
            @Override
            protected boolean matchesSafely(WebDriver item) {
                try {
                    item.findElements(selector);
                    return true;
                } catch (NoSuchElementException _) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description d) {
                d.appendText("contains element that matches ").appendValue(selector);
            }

            @Override
            protected void describeMismatchSafely(WebDriver item, Description d) {
                d.appendText("was at ").appendValue(item.getCurrentUrl());
            }
        };
    }
}
