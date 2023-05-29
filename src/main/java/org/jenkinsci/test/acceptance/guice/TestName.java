package org.jenkinsci.test.acceptance.guice;

import jakarta.inject.Provider;

/**
 * Keeps track of the current test name.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class TestName implements Provider<String> {
    /*package*/ String testName;

    public TestName() {
    }

    public TestName(String testName) {
        this.testName = testName;
    }

    @Override
    public String get() {
        return testName;
    }
}
