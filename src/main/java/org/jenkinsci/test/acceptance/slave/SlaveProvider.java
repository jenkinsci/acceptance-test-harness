package org.jenkinsci.test.acceptance.slave;

import org.jenkinsci.test.acceptance.guice.TestCleaner;

import javax.inject.Provider;

/**
 * @author Kohsuke Kawaguchi
 */
public interface SlaveProvider extends Provider<SlaveController> {
    /**
     * Implementation is responsible for hooking up {@link SlaveController} for {@link TestCleaner}
     */
    @Override
    SlaveController get();
}
