package org.jenkinsci.test.acceptance.guice;

import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Represents a parallel Guice {@link Injector} inside {@link World}
 * so that components can be selectively bound to {@link World}.
 *
 * <p>
 * See WIRING.md
 *
 * @author Kohsuke Kawaguchi
 */
public class SubWorld {
    /*package*/ Injector injector;
    /*package*/ String name;

    /*package*/ SubWorld() {}

    public String getName() {
        return name;
    }

    public Injector getInjector() {
        return injector;
    }

    /**
     * This is a part of the DSL construct that allows people to say:
     *
     * <pre>
     * subworld "masters" {
     *     ...
     *     bind Foo to ...
     * }
     *
     * bind Foo toProvider masters[Foo]  // export Foo from the "masters" subworld to the parent
     * </pre>
     */
    public <T> Provider<T> getAt(Class<T> t) {
        return injector.getProvider(t);
    }
}
