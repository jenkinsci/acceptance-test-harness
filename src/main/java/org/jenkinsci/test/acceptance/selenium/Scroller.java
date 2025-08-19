package org.jenkinsci.test.acceptance.selenium;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * Automatically scrolls the element into view.
 *
 * <p>
 * Especially in the configuration page, the floating DIVs at the top and
 * the bottom of the pages can interfere with WebDriver trying to click
 * the elements underneath it.
 * <p>
 * At least on Chrome (and possibly in other browsers), trying to interact
 * with an element when it's below another element causes the following error:
 *
 * <pre>
 * {@code
 * Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 227.711 sec <<< FAILURE! - in plugins.AntPluginTest
 * autoInstallAnt(plugins.AntPluginTest)  Time elapsed: 73.932 sec  <<< ERROR!
 * org.openqa.selenium.WebDriverException: unknown error: Element is not clickable at point (506, 967). Other element would receive the click: <div class="bottom-sticker-inner">...</div>
 *   (Session info: chrome=34.0.1847.116)
 *   (Driver info: chromedriver=2.10.267518,platform=Linux 3.13.0-24-generic x86_64) (WARNING: The server did not provide any stacktrace information)
 * Command duration or timeout: 61 milliseconds
 * Build info: version: '2.40.0', revision: '4c5c0568b004f67810ee41c459549aa4b09c651e', time: '2014-02-19 11:13:01'
 * System info: host: 'jglick-t520', ip: '127.0.1.1', os.name: 'Linux', os.arch: 'amd64', os.version: '3.13.0-24-generic', java.version: '1.7.0_55'
 * Session ID: 412a7b3ab0eb823da2cddd156e8d122c
 * Driver info: org.openqa.selenium.chrome.ChromeDriver
 * Capabilities [{platform=LINUX, acceptSslCerts=true, javascriptEnabled=true, browserName=chrome, chrome={userDataDir=/tmp/.com.google.Chrome.3Xt0hp}, rotatable=false, locationContextEnabled=true, version=34.0.1847.116, takesHeapSnapshot=true, cssSelectorsEnabled=true, databaseEnabled=false, handlesAlerts=true, browserConnectionEnabled=false, nativeEvents=true, webStorageEnabled=true, applicationCacheEnabled=false, takesScreenshot=true}]
 * 	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
 * 	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:57)
 * 	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
 * 	at java.lang.reflect.Constructor.newInstance(Constructor.java:526)
 * 	at org.openqa.selenium.remote.ErrorHandler.createThrowable(ErrorHandler.java:193)
 * 	at org.openqa.selenium.remote.ErrorHandler.throwIfResponseFailed(ErrorHandler.java:145)
 * 	at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:573)
 * 	at org.openqa.selenium.remote.RemoteWebElement.execute(RemoteWebElement.java:268)
 * 	at org.openqa.selenium.remote.RemoteWebElement.click(RemoteWebElement.java:79)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
 * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 * 	at java.lang.reflect.Method.invoke(Method.java:606)
 * 	at org.openqa.selenium.support.events.EventFiringWebDriver$EventFiringWebElement$1.invoke(EventFiringWebDriver.java:331)
 * 	at com.sun.proxy.$Proxy33.click(Unknown Source)
 * 	at org.openqa.selenium.support.events.EventFiringWebDriver$EventFiringWebElement.click(EventFiringWebDriver.java:344)
 * 	at org.jenkinsci.test.acceptance.po.CapybaraPortingLayer.clickButton(CapybaraPortingLayer.java:66)
 * 	at org.jenkinsci.test.acceptance.po.JenkinsConfig.addTool(JenkinsConfig.java:35)
 * 	at org.jenkinsci.test.acceptance.plugins.ant.AntInstallation.install(AntInstallation.java:37)
 * 	at plugins.AntPluginTest.autoInstallAnt(AntPluginTest.java:76)
 * 	}
 * </pre>
 *
 * <p>
 * This work around simply tries to scroll the element into a view before we interact with this.
 * Originally developed in Ruby version of selenium-tests in {@code lib/jenkins/capybara.rb}.
 *
 * @author ogondza
 * @author Kohsuke Kawaguchi
 */
public class Scroller implements WebDriverListener {

    private final Logger LOGGER = Logger.getLogger(Scroller.class.getName());

    private final String disableStickyElementsJs;
    private final WebDriver driver;

    public Scroller(WebDriver driver) {
        this.driver = driver;
        try {
            disableStickyElementsJs = IOUtils.toString(
                    getClass().getResourceAsStream("disable-sticky-elements.js"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Error("Failed to load the JavaScript file", e);
        }
    }

    @Override
    public void beforeClick(WebElement element) {
        scrollIntoView(element);
    }

    @Override
    public void beforeSendKeys(WebElement element, CharSequence[] keysToSend) {
        scrollIntoView(element);
    }

    @Override
    public void beforeClear(WebElement element) {
        scrollIntoView(element);
    }

    @Override
    public void afterGet(WebDriver driver, String url) {
        try {
            // if there's an alert we can't run JavaScript
            // if we catch the exception from running JavaScript then FormValidationTest hangs
            // not the nicest hack, but it works
            driver.switchTo().alert();
        } catch (NoAlertPresentException e) {
            disableStickyElements();
        }
    }

    /**
     * Sometimes sticky elements (elements that are fixed in position on the page, such as the bottom app bar),
     * appear on top of other elements, making those elements inaccessible. This method removes the sticky
     * nature of these elements meaning that they'll no longer appear on top of other elements.
     */
    public void disableStickyElements() {
        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        try {
            executor.executeScript(disableStickyElementsJs);
        } catch (UnhandledAlertException ignored) {
            // if we're in the process of navigating but an alert is in the way we can't run JS
        }
    }

    /**
     * The framework is expected to take care of the correct scrolling. When you are tempted to scroll from PageObjects
     * or tests, there is likely a framework problem to be fixed.
     */
    public void scrollIntoView(WebElement e) {
        WebElement element = e;
        if (Objects.equals(element.getTagName(), "option")) {
            element = e.findElement(By.xpath("..")); // scroll select into view not option
        }

        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        // Wait until web element is successfully scrolled.
        try {
            executor.executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
        } catch (JavascriptException ex) {
            // Scrolling failed, but sometimes the element to click is already visible, let the test continue and
            // eventually fail later
            // This log message should be sufficient to diagnose the issue
            LOGGER.log(
                    Level.WARNING,
                    "Scrolling failed, letting the test to continue anyways, but \"Element is not clickable\" error will likely be thrown",
                    ex);
        }
    }
}
