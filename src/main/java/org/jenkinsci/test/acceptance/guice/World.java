package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import jakarta.inject.Singleton;
import java.util.List;
import org.jenkinsci.test.acceptance.Config;
import org.jenkinsci.test.acceptance.FallbackConfig;
import org.junit.runners.model.MultipleFailureException;

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
        if (injector == null) {
            injector = Guice.createInjector(this);
        }
        return injector;
    }

    /**
     * Call this method when a new test starts, to reset the {@link TestScope}.
     */
    public void startTestScope(String testName) {
        startTestScope(getInjector(), testName);

        for (SubWorld sw : subworlds.list(injector)) {
            startTestScope(sw.injector, testName);
        }
    }

    private void startTestScope(Injector i, String testName) {
        i.getInstance(TestLifecycle.class).startTestScope();
        i.getInstance(TestName.class).testName = testName;
    }

    public void endTestScope() throws Exception {
        List<Throwable> errors = injector.getInstance(TestCleaner.class).performCleanUp();
        injector.getInstance(TestLifecycle.class).endTestScope();

        for (SubWorld sw : subworlds.list(injector)) {
            errors.addAll(sw.injector.getInstance(TestCleaner.class).performCleanUp());
            sw.injector.getInstance(TestLifecycle.class).endTestScope();
        }

        MultipleFailureException.assertEmpty(errors);
    }

    @Override
    protected void configure() {
        // lowest priority is our default binding
        Module m = new FallbackConfig();

        // let extensions override the fallback config
        m = Modules.override(m).with(new ExtensionFinder(cl));

        // user config trumps everything
        m = Modules.override(m).with(new Config());

        install(m);
    }

    /**
     * Runs at the end of JVM session to clean up.
     */
    private final Thread cleaner = new Thread("World cleaner thread") {
        @Override
        public void run() {
            Injector i = INSTANCE.getInjector();

            TestCleaner tc = i.getInstance(TestCleaner.class);
            if (tc != null) {
                tc.performCleanUp();
            }
            i.getInstance(WorldCleaner.class).performCleanUp();

            for (SubWorld sw : subworlds.list(i)) {
                sw.injector.getInstance(WorldCleaner.class).performCleanUp();
            }
        }
    };

    private static World INSTANCE;

    public static World get() {
        if (INSTANCE == null) {
            INSTANCE = new World(Thread.currentThread().getContextClassLoader());
            Runtime.getRuntime().addShutdownHook(INSTANCE.cleaner);
        }
        return INSTANCE;
    }
}
