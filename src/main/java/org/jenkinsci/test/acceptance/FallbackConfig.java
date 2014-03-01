package org.jenkinsci.test.acceptance;

import com.google.inject.AbstractModule;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.WinstoneController;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

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
            // for now, bind WebDriver to Firefox
            bind(WebDriver.class).toInstance(new FirefoxDriver());

            // bind from scaffolding
            bind(JenkinsController.class).toInstance(new WinstoneController());
        } catch (Exception e) {
            throw new Error("Failed to configure Jenkins acceptance test harness",e);
        }
    }
}
