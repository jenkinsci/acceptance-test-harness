package org.jenkinsci.test.acceptance.guice;

/**
 * {@link Cleaner} at the end of each {@link TestScope}.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class TestCleaner extends Cleaner {
}
