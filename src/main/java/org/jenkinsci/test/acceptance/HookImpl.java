package org.jenkinsci.test.acceptance;

import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 * @author Kohsuke Kawaguchi
 */
public class HookImpl {
    @Before
    public void foo() {
        System.out.println("before");
    }

    @After
    public void bar() {
        System.out.println("after");
    }
}
