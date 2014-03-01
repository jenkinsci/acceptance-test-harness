package org.jenkinsci.test.acceptance;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.ScaffoldController;
import org.jenkinsci.test.acceptance.controller.TomcatController;
import org.jenkinsci.test.acceptance.controller.WinstoneController;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;

/**
 * Scaffolding to get {@link WebDriver} until JenkinsController is ported.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class Scaffold extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        try {
            // for now, bind WebDriver to Firefox
            bind(WebDriver.class).toInstance(new FirefoxDriver());

            // bind from scaffolding
            bind(JenkinsController.class).toInstance(new WinstoneController());
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
