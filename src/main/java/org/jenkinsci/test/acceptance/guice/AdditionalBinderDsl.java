package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import org.jenkinsci.groovy.binder.BinderClosureScript;

import java.util.HashSet;
import java.util.Set;

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
    public Injector subworld(final String name, final Closure config) {
        final Binder binder = getBinder();
        final SubWorld sub = new SubWorld(name);
        try {
            World w = World.get();
            Injector i = Guice.createInjector(
                    Modules.override(
                            new ExtensionFinder(w.getClassLoader())
                    ).with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            AdditionalBinderDsl.this.setBinder(binder());
                            config.setDelegate(sub);
                            config.run();
                        }
                    }));
            // expose this injector
            binder.bind(Injector.class).annotatedWith(Names.named(name)).toInstance(i);

            // export all that's promised
            for (Key key : sub.keys) {
                binder.bind(Key.get(key.getTypeLiteral(), Names.named(name))).toProvider(i.getProvider(key));
            }

            return i;
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

    public class SubWorld extends GroovyObjectSupport {
        private final String name;
        private final Set<Key> keys = new HashSet<>();


        public SubWorld(String name) {
            this.name = name;
        }

        @Override
        public Object getProperty(String property) {
            return AdditionalBinderDsl.this.getProperty(property);
        }

        @Override
        public void setProperty(String property, Object newValue) {
            AdditionalBinderDsl.this.setProperty(property, newValue);
        }

        @Override
        public Object invokeMethod(String name, Object args) {
            try {
                return super.invokeMethod(name, args);
            } catch (MissingMethodException e) {
                return AdditionalBinderDsl.this.invokeMethod(name, args);
            }
        }

        public void export(Class type) {
            export(Key.get(type));
        }

        public void export(Key key) {
            keys.add(key);
        }
    }
}
