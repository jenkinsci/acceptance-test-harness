package org.jenkinsci.test.acceptance.cucumber;

import cucumber.api.java.After;
import cucumber.api.java.Before;
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

    @Before
    public void before() {

        System.out.println("before");
    }

    @After
    public void bar() {
        driver.close();
        System.out.println("after");
    }
}
