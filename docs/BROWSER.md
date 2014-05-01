# Selecting Browser

This test harness uses WebDriver to execute tests, and the person who runs tests can select what browser to use
by using the `BROWSER` environment variable. The following values are available:

 * `firefox` (default)
 * `ie`
 * `chrome`
 * `safari`
 * `htmlunit`
 * `phantomjs`

Therefore, to run tests with Safari, you'd execute:

    export BROWSER=safari
    mvn install

    # or more concisely
    BROWSER=safari mvn install

See `FallbackConfig.java` for how the browser is selected.

TODO: port over Sauce OnDemand support.

## Advanced Browser Configuration
[This test harness internally uses Guice](GUICE.md) to wire tests, and that is how we control
WebDriver. To further fine-tune how a browser is selected and configured, bind `WebDriver` to
a specific factory of your choice.

Usually you'd bind WebDriver to test scope so that each test gets a new browser instance.
For example,

    bind WebDriver toProvider {
        def d = new FirefoxDriver();
        d.manage().addCookie(...);
        return d;
    } as Provider _in TestScope

See [WIRING.md](WIRING.md) for details of where to put this.