package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.Config;

import javax.inject.Singleton;

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

    private final ExtensionList<SubWorld> subworlds = new ExtensionList<>(SubWorld.class);

    private World(ClassLoader cl) {
        this.cl = cl;
    }

    public ClassLoader getClassLoader() {
        return cl;
    }

    public Injector getInjector() {
        if (injector==null)
            injector = Guice.createInjector(this);
        return injector;
    }

    /**
     * Call this method when a new test starts, to reset the {@link TestScope}.
     */
    public void startTestScope() {
        injector.getInstance(TestLifecycle.class).startTestScope();

        for (SubWorld sw : subworlds.list(injector)) {
            sw.injector.getInstance(TestLifecycle.class).startTestScope();
        }
    }

    public void endTestScope() {
        injector.getInstance(TestCleaner.class).performCleanUp();
        injector.getInstance(TestLifecycle.class).endTestScope();

        for (SubWorld sw : subworlds.list(injector)) {
            sw.injector.getInstance(TestCleaner.class).performCleanUp();
            sw.injector.getInstance(TestLifecycle.class).endTestScope();
        }
    }

    @Override
    protected void configure() {
        install(new ExtensionFinder(cl));
        install(new Config());
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
