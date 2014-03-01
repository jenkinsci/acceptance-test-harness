package org.jenkinsci.test.acceptance;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.WebDriver;

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

}
