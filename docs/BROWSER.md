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
If you select a real GUI browser, such as Firefox, browser window will pop up left and right during tests, making it practically unusable for you to use your computer. There is a script to run vnc server and propage the display number to the test suite.

    $ . vnc.sh
    $ mvn test

## Example on using remote web driver

Non tested pseudo bash example

    docker run -d -P selenium/standalone-firefox-debug > containerId.txt
    export WEBDRIVER_CONTAINER_ID=$(cat containerId.txt)
    export BROWSER=remote-webdriver-firefox
    export REMOTE_WEBDRIVER_URL=http://$(docker port $WEBDRIVER_CONTAINER_ID 4444)/wd/hub
    export JENKINS_LOCAL_HOSTNAME=$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')
    mvn test

