# Selecting Browser

This test harness uses WebDriver to execute tests, and the person who runs tests can select what browser to use
by using the `BROWSER` environment variable. The following values are available:

 * `firefox` (default)
 * `ie`
 * `chrome`
 * `safari`
 * `htmlunit`
 * `phantomjs`
 * `saucelabs`
 * `remote-webdriver-firefox` 
        _(Needs `REMOTE_WEBDRIVER_URL` to also be set to the url of the remote, for example `http://0.0.0.0:32779/wd/hub`
          when using something like [selenium/standalone-firefox-debug](https://hub.docker.com/r/selenium/standalone-firefox-debug/))_

Therefore, to run tests with Safari, you'd execute:

    export BROWSER=safari
    mvn install

    # or more concisely
    BROWSER=safari mvn install

See `FallbackConfig.java` for how the browser is selected.

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

## Avoid focus steal with Xvnc on Linux
If you select a real GUI browser, such as Firefox, browser window will pop up left and right during tests,
making it practically unusable for you to use your computer.

To prevent this, you can use [Xvnc](http://www.hep.phy.cam.ac.uk/vnc_docs/xvnc.html), so that the browser under the test
will go to a separate display. For example, on Ubuntu you can run `vncserver`, then run tests like the following:

    $ vncserver    
    ...
    New 'X' desktop is elf:1
    $ DISPLAY=elf:1 mvn test 

