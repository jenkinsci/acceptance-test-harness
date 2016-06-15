/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.test.acceptance;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestName;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.selenium.SanityChecker;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.jenkinsci.test.acceptance.utils.SauceLabsConnection;
import org.junit.runners.model.Statement;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.google.inject.Inject;

/**
 * Provider to create a WebDriver instance, allowing injection of Firefox profile
 * 
 * @author Carlos Sanchez
 *
 */
public class WebDriverProvider implements Provider<WebDriver>, com.google.inject.Provider<WebDriver> {

    /** Browser property to set the default locale. */
    private static final String LANGUAGE_SELECTOR = "intl.accept_languages";

    /**
     * PhantomJS browser property to set the default locale.
     */
    private static final String LANGUAGE_SELECTOR_PHANTOMJS = "phantomjs.page.customHeaders.Accept-Language";

    @Inject
    private TestCleaner cleaner;

    @Inject
    private TestName testName;

    @Inject
    private ElasticTime time;

    @Inject
    private FirefoxProfile firefoxProfile;

    private WebDriver createWebDriver(TestName testName) throws IOException {
        String browser = System.getenv("BROWSER");
        if (browser==null) browser = "firefox";
        browser = browser.toLowerCase(Locale.ENGLISH);

        switch (browser) {
        case "firefox":
            if (firefoxProfile == null) {
                firefoxProfile = new FirefoxProfile();
            }
            firefoxProfile.setAlwaysLoadNoFocusLib(true);

            if (firefoxProfile.getStringPreference(LANGUAGE_SELECTOR, null) == null) {
                firefoxProfile.setPreference(LANGUAGE_SELECTOR, "en");
            }

            return new FirefoxDriver(firefoxProfile);
        case "ie":
        case "iexplore":
        case "iexplorer":
            return new InternetExplorerDriver();
        case "chrome":
            Map<String, String> prefs = new HashMap<String, String>();
            prefs.put(LANGUAGE_SELECTOR, "en");
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);

            return new ChromeDriver(options);
        case "safari":
            return new SafariDriver();
        case "htmlunit":
            return new HtmlUnitDriver(true);
        case "saucelabs":
        case "saucelabs-firefox":
            DesiredCapabilities caps = DesiredCapabilities.firefox();
            caps.setCapability("version", "29");
            caps.setCapability("platform", "Windows 7");
            caps.setCapability("name", testName.get());

            // if running inside Jenkins, expose build ID
            String tag = System.getenv("BUILD_TAG");
            if (tag!=null)
                caps.setCapability("build", tag);

            return new SauceLabsConnection().createWebDriver(caps);
        case "phantomjs":
            DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
            capabilities.setCapability(LANGUAGE_SELECTOR, "en");
            capabilities.setCapability(LANGUAGE_SELECTOR_PHANTOMJS, "en");
            return new PhantomJSDriver(capabilities);
        case "remote-webdriver-firefox":
            String u = System.getenv("REMOTE_WEBDRIVER_URL");
            if (StringUtils.isBlank(u)) {
                throw new Error("remote-webdriver-firefox requires REMOTE_WEBDRIVER_URL to be set");
            }
            return new RemoteWebDriver(
                    new URL(u), //http://192.168.99.100:4444/wd/hub
                    DesiredCapabilities.firefox());

        default:
            throw new Error("Unrecognized browser type: "+browser);
        }
    }

    /**
     * Creates a {@link WebDriver} for each test, then make sure to clean it up at the end.
     */
    @TestScope
    @Override
    public WebDriver get() {
        WebDriver base;
        try {
            base = createWebDriver(testName);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

        // Make sue the window have minimal resolution set, even when out of the visible screen. Try maximizing first so
        // it has a chance to fit the screen nicely if big enough.
        base.manage().window().maximize();
        Dimension oldSize = base.manage().window().getSize();
        if (oldSize.height < 960 || oldSize.width < 1280) {
            base.manage().window().setSize(new Dimension(1280, 960));
        }

        final EventFiringWebDriver d = new EventFiringWebDriver(base);
        d.register(new SanityChecker());
        try {
            d.register(new Scroller());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            d.manage().timeouts().pageLoadTimeout(time.seconds(30), TimeUnit.MILLISECONDS);
            d.manage().timeouts().implicitlyWait(time.seconds(1), TimeUnit.MILLISECONDS);
        } catch (UnsupportedCommandException e) {
            // sauce labs RemoteWebDriver doesn't support this
            System.out.println(base + " doesn't support page load timeout");
        }
        cleaner.addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    d.quit();
                } catch (UnreachableBrowserException ex) {
                    System.err.println("Browser died already");
                    ex.printStackTrace();
                }
            }

            @Override public String toString() {
                return "Close WebDriver after test";
            }
        });
        return d;
    }

}
