package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import groovy.lang.Binding;
import groovy.lang.Closure;
import org.jenkinsci.groovy.binder.BinderClosureScript;

/**
 * Enhance Groovy binder DSL with additional methods.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AdditionalBinderDsl extends BinderClosureScript {
    public AdditionalBinderDsl() {
    }

    public AdditionalBinderDsl(Binding binding) {
        super(binding);
    }

    /**
     * Creates another injector and bind them using the given closure.
     *
     * The newly created injector will be returned.
     */
    public SubWorld subworld(final String name, final Closure config) {
        final Binder binder = getBinder();
//        final SubWorldBuilder sub = new SubWorldBuilder(name);
        try {
            World w = World.get();
            Injector i = Guice.createInjector(
                    Modules.override(
                            new ExtensionFinder(w.getClassLoader())
                    ).with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            AdditionalBinderDsl.this.setBinder(binder());
                            config.setDelegate(AdditionalBinderDsl.this);
                            config.run();
                        }
                    }));
            SubWorld sw = new SubWorld(name, i);

            // expose this world
            binder.bind(SubWorld.class).annotatedWith(Names.named(name)).toInstance(sw);

            // expose the subworld by its name to the rest of the script
            getBinding().setProperty(name,sw);

            return sw;
        } catch (CreationException e) {
            throw new RuntimeException("Failed to create a sub-world "+name,e);
        } finally {
            setBinder(binder);
        }
    }

    /**
     * Installs the given binding DSL closure as a module.
     */
    public void install(Closure c) {
        getBinder().install(module(c));
    }

    /**
     * Given a closure that uses Groovy binder DSL, wrap that into a Guice {@link Module}.
     *
     * <p>
     * To reuse the DSL methods defined on {@link AdditionalBinderDsl} and its super types,
     * while we call the closure we temporarily swap {@link Binder} object.
     */
    public Module module(final Closure c) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                final Binder old = getBinder();
                try {
                    c.setDelegate(binder());
                    c.call();
                } finally {
                    setBinder(old);
                }
            }
        };
    }
}
