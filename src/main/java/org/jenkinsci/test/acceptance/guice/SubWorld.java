package org.jenkinsci.test.acceptance.guice;

import com.google.inject.Injector;
import com.google.inject.Provider;
import org.jenkinsci.test.acceptance.controller.MachineProvider;

/**
 * Represents a parallel Guice {@link Injector} inside {@link World}
 * so that components can be selectively bound to {@link World}.
 *
 * <p>
 * For example, this mechanism allows sophisticated test launcher configurations to
 * use different {@link MachineProvider} implementations for master and slaves, etc.
 *
 * <p>
 * See WIRING.md
 *
 * @author Kohsuke Kawaguchi
 */
public class SubWorld {
    public final Injector injector;
    private String name;

    /*package*/ SubWorld(Injector injector) {
        this.injector = injector;
    }

    public String getName() {
        return name;
    }

    /*package*/ void setName(String name) {
        this.name = name;
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
