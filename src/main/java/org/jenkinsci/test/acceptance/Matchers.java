package org.jenkinsci.test.acceptance;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

/**
 * Hamcrest matchers.
 *
 * @author Kohsuke Kawaguchi
 */
public class Matchers {
    /**
     * Asserts that given text is shown on page.
     */
    public static Matcher<WebDriver> hasContent(final String content) {
        return hasContent(Pattern.compile(Pattern.quote(content)));
    }

    public static Matcher<WebDriver> hasContent(final Pattern pattern) {
      return new Matcher<WebDriver>("Text matching %s", pattern) {
          @Override
          protected boolean matchesSafely(WebDriver item) {
              return pattern.matcher(pageText(item)).find();
          }

          @Override
          protected void describeMismatchSafely(WebDriver item, Description mismatchDescription) {
              mismatchDescription.appendText("was ")
                      .appendValue(item.getCurrentUrl())
                      .appendText("\n")
                      .appendValue(pageText(item));
          }

          private String pageText(WebDriver item) {
              return item.findElement(by.xpath("/html")).getText();
          }
      };
    }

    /**
     * Matches that matches {@link WebDriver} when it has an element that matches to the given selector.
     */
    public static Matcher<WebDriver> hasElement(final By selector) {
        return new Matcher<WebDriver>("contains element that matches %s", selector) {
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
            protected void describeMismatchSafely(WebDriver item, Description d) {
                d.appendText("was at ").appendValue(item.getCurrentUrl());
            }
        };
    }

    /**
     * For asserting that a {@link PageObject}'s top page has an action of the given name.
     */
    public static Matcher<PageObject> hasAction(final String displayName) {
        return new Matcher<PageObject>("contains action titled %s", displayName) {
            @Override
            protected boolean matchesSafely(PageObject po) {
                try {
                    po.open();
                    po.find(by.xpath("//div[@id='tasks']/div/a[text()='%s']", displayName));
                    return true;
                } catch (NoSuchElementException _) {
                    return false;
                }
            }

            @Override
            protected void describeMismatchSafely(PageObject po, Description d) {
                d.appendValue(po.url).appendText(" does not have action: ").appendValue(displayName);
            }
        };
    }
    public static Matcher<String> containsRegexp(String regexp) {
        return containsRegexp(regexp,0);
    }

    /**
     * Matches if a string contains a portion that matches to the regular expression.
     */
    public static Matcher<String> containsRegexp(final String regexp, int opts) {
        final Pattern re = Pattern.compile(regexp, opts);

        return new Matcher<String>("Matches regexp %s", regexp) {
            @Override
            protected boolean matchesSafely(String item) {
                return re.matcher(item).find();
            }
        };
    }

    public static Matcher<ContainerPageObject> pageObjectExists(){
        return new Matcher<ContainerPageObject>("Page object exists") {
            @Override
            protected void describeMismatchSafely(ContainerPageObject item, Description desc) {
                desc.appendText(item.url.toString()).appendText(" does not exist");
            }

            @Override protected boolean matchesSafely(ContainerPageObject item) {
                try {
                    item.getJson();
                    return true;
                } catch (RuntimeException ex) {
                    return false;
                }
            }
        };
    }

    public static final ByFactory by = new ByFactory();
}
