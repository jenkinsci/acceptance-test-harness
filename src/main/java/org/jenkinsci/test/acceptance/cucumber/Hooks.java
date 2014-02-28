package org.jenkinsci.test.acceptance.cucumber;

import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class Hooks {
    @Before
    public void before() {

        System.out.println("before");
    }

    @After
    public void bar() {
        System.out.println("after");
    }
}
