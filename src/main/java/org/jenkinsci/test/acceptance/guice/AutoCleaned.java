package org.jenkinsci.test.acceptance.guice;

import java.io.Closeable;

/**
 * Marks instances that want to run some shutdown action
 * at the end of their scope.
 * <p>
 * When a test scope exits, all the existing instances that implement this interface
 * gets its {@link #close()} method invoked.
 *
 * <p>
 * Currently this only works with {@link TestScope}.
 *
 * @see TestCleaner
 * @author Kohsuke Kawaguchi
 */
public interface AutoCleaned extends Closeable {}
