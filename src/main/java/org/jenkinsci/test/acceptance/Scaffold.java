package org.jenkinsci.test.acceptance;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Scaffolding to get {@link WebDriver} until JenkinsController is ported.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class Scaffold extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        // for now, bind WebDriver to Firefox
        bind(WebDriver.class).toInstance(new FirefoxDriver());


    }
}
