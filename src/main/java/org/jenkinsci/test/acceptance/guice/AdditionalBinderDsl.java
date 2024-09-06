package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Guice;
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
    public AdditionalBinderDsl() {}

    public AdditionalBinderDsl(Binding binding) {
        super(binding);
    }

    /**
     * Creates another injector and bind them using the given closure.
     * <p>
     * The newly created injector will be returned.
     */
    public SubWorld subworld(final Closure config) {
        final Binder binder = getBinder();
        //        final SubWorldBuilder sub = new SubWorldBuilder(name);
        try {
            final SubWorld sw = new SubWorld();

            World w = World.get();
            sw.injector = Guice.createInjector(
                    Modules.override(new ExtensionFinder(w.getClassLoader())).with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            AdditionalBinderDsl.this.setBinder(binder());
                            config.setDelegate(AdditionalBinderDsl.this);
                            config.run();
                            bind(SubWorld.class).toInstance(sw);
                        }
                    }));

            return sw;
        } catch (CreationException e) {
            throw new RuntimeException("Failed to create a sub-world", e);
        } finally {
            setBinder(binder);
        }
    }

    @Override
    public void setProperty(String property, Object value) {
        if (value instanceof SubWorld) {
            ((SubWorld) value).name = property;
        }
        super.setProperty(property, value);
    }
}
