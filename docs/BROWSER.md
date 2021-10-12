# Selecting Browser

This test harness uses WebDriver to execute tests.
The person who runs tests can select which browser to use by using the `BROWSER` environment variable.
The following values are available:

 * `firefox` (default)
        _(if set the binary at `FIREFOX_BIN` will be used to launch firefox)_
 * `ie`
 * `chrome`
 * `safari`
 * `htmlunit`
 * `phantomjs`
 * `saucelabs`
 * `remote-webdriver-firefox`
        _(Requires `REMOTE_WEBDRIVER_URL` to be set to the url of the remote,
          for example `http://0.0.0.0:32779/wd/hub`
          when using something like
          [selenium/standalone-firefox-debug](https://hub.docker.com/r/selenium/standalone-firefox-debug/))_
 * `remote-webdriver-chrome`
        _(Requires `REMOTE_WEBDRIVER_URL` to be set to the url of the remote,
          for example `http://0.0.0.0:32779/wd/hub`
          when using something like
          [selenium/standalone-chrome-debug](https://hub.docker.com/r/selenium/standalone-chrome-debug/))_
 * `firefox-container` and `chrome-container`
        Running the browser inside selenium provided per-test container.

For example, to run tests with Safari, you'd execute:

    export BROWSER=safari
    mvn install

    # or more concisely
    BROWSER=safari mvn install

See `FallbackConfig.java` for how the browser is selected.

Please note selenium library is sensitive to versions of browser used so it is better to stick with recent stable versions of mainstream web browsers. For more information about Selenium supported platforms visit [this page](http://www.seleniumhq.org/about/platforms.jsp).

## Advanced Browser Configuration
[This test harness internally uses Guice](GUICE.md) to wire components, and that is how we control
WebDriver. To further fine-tune how a browser is selected and configured, bind `WebDriver` to
a specific factory of your choice.

Usually you'd bind WebDriver to the test scope so that each test gets a new browser instance.
For example,

    bind WebDriver toProvider {
        def d = new FirefoxDriver();
        d.manage().addCookie(...);
        return d;
    } as Provider _in TestScope

See [WIRING.md](WIRING.md) for details of where to put this.

## Recording network interactions

Network interactions between the browser and the Jenkins instance are recorded by default and saved on failures.

This works by setting up a proxy recording everything that goes through it and then configure the browser to use it.
Supported drivers are: `firefox`, `chrome`, and `saucelabs-firefox`

This feature can be disabled using 

    RECORD_BROWSER_TRAFFIC=off mvn install
    
It can also be set to save results even on success using

    RECORD_BROWSER_TRAFFIC=always mvn install

If the host running maven is different to the host running Selenium (e.g. `remote-webdriver-selenium`) then you may have to specify the network address to use for the proxy (by default it will bind to 127.0.0.1 which would not be reachable for the browser).
If this is the case you can specify the address to use using:
    `SELENIUM_PROXY_HOSTNAME=ip.address.of.host mvn install`
**Important**: this could exposed the proxy wider beyond your machine and expose other internal services, so this should only be used on private or internal networks to prevent any information leak.

## Avoid focus steal with Xvnc on Linux
If you select a real GUI browser, such as Firefox,
a browser window will pop up left and right during tests,
making it practically unusable for you to use your computer.
There is a script to run VNC server and propagate the display number to the test suite using the dedicated variable `BROWSER_DISPLAY`.

    $ eval "$(./vnc.sh)"
    $ mvn test

## Example using remote web driver

Untested pseudo bash example

    docker run --shm-size=256m -d -P selenium/standalone-firefox-debug > containerId.txt
    export WEBDRIVER_CONTAINER_ID=$(cat containerId.txt)
    export BROWSER=remote-webdriver-firefox
    export REMOTE_WEBDRIVER_URL=http://$(docker port $WEBDRIVER_CONTAINER_ID 4444)/wd/hub
    export JENKINS_LOCAL_HOSTNAME=$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')
    mvn test

It is important to use more that the default 64m of shared memory for firefox to avoid crashes like [this](https://bugzilla.mozilla.org/show_bug.cgi?id=1245239) and [this](https://bugzilla.mozilla.org/show_bug.cgi?id=1338771#c10)
