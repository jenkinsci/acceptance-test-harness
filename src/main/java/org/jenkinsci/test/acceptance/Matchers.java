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
    public static Matcher<WebDriver> hasElementOf(final By selector) {
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

    /**
     * In several step definitions, the assertion gets negated depending on whether
     * or not the step is "should" or "should not". This method decorates the given matcher
     * accordingly.
     *
     * @see JobSteps#the_artifact_should_be_archived
     */
    public static <T> Matcher<T> dependingOn(String shouldOrNot, Matcher<T> actual) {
        if (shouldOrNot.equals("should"))
            return actual;
        if (shouldOrNot.equals("should not"))
            return not(actual);

        throw new AssertionError("Unecpted matcher token: "+shouldOrNot);
    }
}
