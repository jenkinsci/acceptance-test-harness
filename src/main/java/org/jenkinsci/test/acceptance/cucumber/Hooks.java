package org.jenkinsci.test.acceptance.cucumber;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class Hooks {
    @Inject
    WebDriver driver;


    @Inject
    JenkinsController jenkinsController;

    @Before
    public void before() {
        jenkinsController.start();

    }

    @After
    public void bar() {
        jenkinsController.stop();
        driver.close();
    }
}
