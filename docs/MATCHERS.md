# Using Hamcrest matchers

Using hamcrest matchers is a preferred way to writing assertions.
See [Matchers](https://github.com/jenkinsci/acceptance-test-harness/blob/master/src/main/java/org/jenkinsci/test/acceptance/Matchers.java)
class for an inspiration of how those can look like.
Note the convenience (typesafe) [Matcher](https://github.com/jenkinsci/acceptance-test-harness/blob/master/src/main/java/org/jenkinsci/test/acceptance/Matcher.java)
superclass we use to avoid unnecessary verbosity:

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

General purpose matchers should be available as static methods of the `Matchers` class.
Plugin specific matchers should be defined in JUnit class or in dedicated `*Matcher` class.
