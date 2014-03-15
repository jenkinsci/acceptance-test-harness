package org.jenkinsci.test.acceptance.slave;

import org.jenkinsci.test.acceptance.guice.TestCleaner;

import javax.inject.Provider;

/**
 * Obtains {@link SlaveController}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface SlaveProvider extends Provider<SlaveController>, com.google.inject.Provider<SlaveController> {
    /**
     * Provisions a new place to run a computer from somewhere and return
     * an object that encapsulates how to let Jenkins connect to it.
     *
     * Implementation is responsible for hooking up {@link SlaveController} for {@link TestCleaner}
     */
    @Override
    SlaveController get();
}
