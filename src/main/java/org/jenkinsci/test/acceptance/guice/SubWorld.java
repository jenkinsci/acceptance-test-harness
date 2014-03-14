package org.jenkinsci.test.acceptance.guice;

import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * @author Kohsuke Kawaguchi
 */
public class SubWorld {
    public final String name;
    public final Injector injector;

    public SubWorld(String name, Injector injector) {
        this.name = name;
        this.injector = injector;
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
     * bind Foo toProvider masters[Foo]  // export Foo from master to its parent
     * </pre>
     */
    public <T> Provider<T> getAt(Class<T> t) {
        return injector.getProvider(t);
    }
}
