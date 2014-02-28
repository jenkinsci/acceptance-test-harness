package org.jenkinsci.test.acceptance.cucumber;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import cucumber.runtime.java.ObjectFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Takes over cucumber object instantiation by Guice to incorporating
 * the cloudbees-extensibility component world.
 *
 * @author Kohsuke Kawaguchi
 */
public class ObjectFactoryImpl implements ObjectFactory {
    private final List<Module> modules = new ArrayList<Module>();
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private Injector injector;

    public ObjectFactoryImpl(Module... modules) {
        Collections.addAll(this.modules, modules);
    }

    public <T> T getInstance(Class<T> type) {
        return injector.getInstance(type);
    }

    public void stop() {
        injector = null;
    }

    public void addClass(Class<?> glue) {
        classes.add(glue);
    }

    public void start() {
        injector = Guice.createInjector(Modules.override(new AbstractModule() {
            @Override
            protected void configure() {
                for (Class<?> c : classes) {
                    bind(c).in(javax.inject.Singleton.class);
                }
            }
        }).with(modules));
    }
}
