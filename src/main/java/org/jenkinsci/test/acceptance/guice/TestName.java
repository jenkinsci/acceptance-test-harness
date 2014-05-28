package org.jenkinsci.test.acceptance.guice;

import javax.inject.Provider;

/**
 * Keeps track of the current test name.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class TestName implements Provider<String> {
    /*package*/ String testName;

    @Override
    public String get() {
        return testName;
    }
}
