package org.jenkinsci.test.acceptance;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.jenkinsci.test.acceptance.controller.ControllerFactory;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.server.JenkinsControllerPoolProcess;
import org.jenkinsci.test.acceptance.server.PooledJenkinsController;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The default configuration for running tests.
 *
 * See {@link Config} for how to override it.
 *
 * @author Kohsuke Kawaguchi
 */
public class FallbackConfig extends AbstractModule {
    @Override
    protected void configure() {
    }

    private WebDriver createWebDriver() {
        String browser = System.getenv("BROWSER");
        if (browser==null)  browser="firefox";
        browser = browser.toLowerCase(Locale.ENGLISH);

        switch (browser) {
        case "firefox":
            return new FirefoxDriver();
        case "ie":
        case "iexplore":
        case "iexplorer":
            return new InternetExplorerDriver();
        case "chrome":
            return new ChromeDriver();
        case "safari":
            return new SafariDriver();
        case "htmlunit":
            return new HtmlUnitDriver();
        default:
            throw new Error("Unrecognized browser type: "+browser);
        }
    }

    /**
     * Creates a {@link WebDriver} for each test, then make sure to clean it up at the end.
     */
    @Provides @TestScope
    public WebDriver createWebDriver(TestCleaner cleaner) {
        final WebDriver d = createWebDriver();
        d.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        cleaner.addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                d.close();
            }
        });
        return d;
    }

    /**
     * Instantiates a controller through the "TYPE" attribute and {@link ControllerFactory}.
     */
    @Provides @TestScope
    public JenkinsController createController(ExtensionList<ControllerFactory> factories, TestCleaner cleaner) throws IOException {
        String type = System.getenv("type");  // this is lower case for backward compatibility
        if (type==null)
            type = System.getenv("TYPE");
        if (type==null) {
            if (JenkinsControllerPoolProcess.SOCKET.exists() && !JenkinsControllerPoolProcess.MAIN)
                return new PooledJenkinsController(JenkinsControllerPoolProcess.SOCKET);
            else
                type = "winstone";
        }

        for (ControllerFactory f : factories) {
            if (f.getId().equalsIgnoreCase(type)) {
                final JenkinsController c = f.create();
                c.start();
                cleaner.addTask(c);

                return c;
            }
        }

        throw new AssertionError("Invalid controller type: "+type);
    }

}
