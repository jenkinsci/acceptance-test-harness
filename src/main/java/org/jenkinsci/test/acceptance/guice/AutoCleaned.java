package org.jenkinsci.test.acceptance.guice;

import java.io.Closeable;

/**
 * Marks {@link TestScope} instances that want to run some shutdown action
 * at the end of the scope.
 *
 * When a test scope exits, all the existing instances that implement this interface
 * gets its {@link #close()} method invoked.
 *
 * @see TestCleaner
 * @author Kohsuke Kawaguchi
 */
public interface AutoCleaned extends Closeable {
}
