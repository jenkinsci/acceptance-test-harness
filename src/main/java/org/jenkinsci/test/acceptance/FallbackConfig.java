package org.jenkinsci.test.acceptance;

import com.google.inject.AbstractModule;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.WinstoneController;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.util.Locale;

/**
 * The default configuration for running tests.
 *
 * See {@link Config} for how to override it. Even when you specify your own config, it still
 * "inherits" from this setting.
 *
 * @author Kohsuke Kawaguchi
 */
public class FallbackConfig extends AbstractModule {
    @Override
    protected void configure() {
        try {
            String browser = System.getenv("BROWSER");
            if (browser==null)  browser="firefox";
            browser = browser.toLowerCase(Locale.ENGLISH);

            switch (browser) {
            case "firefox":
                bind(WebDriver.class).toInstance(new FirefoxDriver());
                break;
            case "ie":
            case "iexplore":
            case "iexplorer":
                bind(WebDriver.class).toInstance(new InternetExplorerDriver());
                break;
            case "chrome":
                bind(WebDriver.class).toInstance(new ChromeDriver());
                break;
            case "safari":
                bind(WebDriver.class).toInstance(new SafariDriver());
                break;
            case "htmlunit":
                bind(WebDriver.class).toInstance(new HtmlUnitDriver());
                break;
            }

            bind(JenkinsController.class).toInstance(new WinstoneController());
        } catch (Exception e) {
            throw new Error("Failed to configure Jenkins acceptance test harness",e);
        }
    }
}
