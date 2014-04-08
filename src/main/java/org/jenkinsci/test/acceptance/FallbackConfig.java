package org.jenkinsci.test.acceptance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsControllerFactory;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.server.JenkinsControllerPoolProcess;
import org.jenkinsci.test.acceptance.server.PooledJenkinsController;
import org.jenkinsci.test.acceptance.slave.LocalSlaveProvider;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.jenkinsci.test.acceptance.utils.SauceLabsConnection;
import org.junit.runners.model.Statement;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * The default configuration for running tests.
 *
 * See {@link Config} for how to override it.
 *
 * @author Kohsuke Kawaguchi
 */
public class FallbackConfig extends AbstractModule {
    /** Browser property to set the default locale. */
    private static final String LANGUAGE_SELECTOR = "intl.accept_languages";

    @Override
    protected void configure() {
        // default in case nothing is specified
        bind(SlaveProvider.class).to(LocalSlaveProvider.class);
    }

    private WebDriver createWebDriver() throws IOException {
        String browser = System.getenv("BROWSER");
        if (browser==null)  browser="firefox";
        browser = browser.toLowerCase(Locale.ENGLISH);


        switch (browser) {
        case "firefox":
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference(LANGUAGE_SELECTOR, "en");

            return new FirefoxDriver(profile);
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
            return new HtmlUnitDriver();
        case "saucelabs":
        case "saucelabs-firefox":
            DesiredCapabilities caps = DesiredCapabilities.firefox();
            caps.setCapability("version", "5");
            caps.setCapability("platform", Platform.WINDOWS);

            return new SauceLabsConnection().createWebDriver(caps);

        default:
            throw new Error("Unrecognized browser type: "+browser);
        }
    }

    /**
     * Creates a {@link WebDriver} for each test, then make sure to clean it up at the end.
     */
    @Provides @TestScope
    public WebDriver createWebDriver(TestCleaner cleaner) throws IOException {
        final EventFiringWebDriver d = new EventFiringWebDriver(createWebDriver());
        d.register(new SanityChecker());

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
     * Instantiates a controller through the "TYPE" attribute and {@link JenkinsControllerFactory}.
     */
    @Provides @TestScope
    public JenkinsController createController(ExtensionList<JenkinsControllerFactory> factories) throws IOException {
        String type = System.getenv("type");  // this is lower case for backward compatibility
        if (type==null)
            type = System.getenv("TYPE");
        if (type==null) {
            if (JenkinsControllerPoolProcess.SOCKET.exists() && !JenkinsControllerPoolProcess.MAIN)
                return new PooledJenkinsController(JenkinsControllerPoolProcess.SOCKET);
            else
                type = "winstone";
        }

        for (JenkinsControllerFactory f : factories) {
            if (f.getId().equalsIgnoreCase(type)) {
                final JenkinsController c = f.create();
                c.start();

                return c;
            }
        }

        throw new AssertionError("Invalid controller type: "+type);
    }
}
