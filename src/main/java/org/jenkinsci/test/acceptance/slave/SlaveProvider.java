package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import jakarta.inject.Provider;
import org.jenkinsci.test.acceptance.guice.TestCleaner;

/**
 * Obtains {@link SlaveController}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class SlaveProvider implements Provider<SlaveController>, com.google.inject.Provider<SlaveController> {
    @Inject
    Provider<TestCleaner> cleaner;

    /**
     * Provisions a new place to run a computer from somewhere and return
     * an object that encapsulates how to let Jenkins connect to it.
     */
    @Override
    public SlaveController get() {
        SlaveController c = create();
        cleaner.get().addTask(c);
        return c;
    }

    /**
     * Actual SPI for {@link #get()} method that subtypes need to implement.
     */
    protected abstract SlaveController create();
}
