package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Holder of the Guice world for running tests. Singleton.
 *
 * <p>
 * Hides the details of how a Guice injector is created.
 *
 * <p>
 * In this test harness, Guice has two important scopes. One is the singleton scope that lives
 * for the entire duration of the VM (thus covering all the tests that are run.) This is the
 * {@link Singleton} scope. The other scope is {@link TestScope}, which is for each test case
 * that runs.
 *
 * @author Kohsuke Kawaguchi
 */
public class World extends AbstractModule {
    private final ClassLoader cl;
    private Injector injector;

    private World(ClassLoader cl) {
        this.cl = cl;
    }

    public Injector getInjector() {
        if (injector==null)
            injector = Guice.createInjector(this);
        return injector;
    }

    /**
     * Records components that are scoped to tests.
     *
     * Inherited, so that threads created from within a test can correctly identify its scope.
     */
    private final ThreadLocal<Map> testScopeObjects = new InheritableThreadLocal<>();

    /**
     * Call this method when a new test starts, to reset the {@link TestScope}.
     */
    public void startTestScope() {
        testScopeObjects.set(new HashMap());
    }

    public void endTestScope() {
        getInjector().getInstance(TestCleaner.class).performCleanUp();
        testScopeObjects.set(null);
    }

    @Override
    protected void configure() {
        install(new ExtensionFinder(cl));
        bindScope(TestScope.class, new Scope() {
            public <T> Provider<T> scope(final Key<T> key, final Provider<T> base) {
                return new Provider<T>() {
                    @Override
                    public T get() {
                        Map m = testScopeObjects.get();
                        if (m==null)    return null;
                        T v = (T)m.get(key);
                        if (v==null)
                            m.put(key, v = base.get());
                        return v;
                    }
                };
            }
        });
    }

    private static World INSTANCE;

    public static World get() {
        if (INSTANCE==null) {
            INSTANCE = new World(Thread.currentThread().getContextClassLoader());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    Injector i = INSTANCE.getInjector();

                    TestCleaner tc = i.getInstance(TestCleaner.class);
                    if (tc!=null)   tc.performCleanUp();
                    i.getInstance(WorldCleaner.class).performCleanUp();
                }
            });
        }
        return INSTANCE;
    }
}
