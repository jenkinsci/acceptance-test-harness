package org.jenkinsci.test.acceptance.junit;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * JUnit test {@link Runner} that uses Guice to instantiate the test class,
 * among with all the components provided by the test harness.
 *
 * @author Kohsuke Kawaguchi
 * @deprecated
 *      Use {@link JenkinsAcceptanceTestRule}
 */
public class JenkinsAcceptanceTestRunner extends BlockJUnit4ClassRunner {
    public JenkinsAcceptanceTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * {@link BlockJUnit4ClassRunner} uses this to instantiate the test class.
     * Here we do so with Guice.
     */
    @Override
    protected Object createTest() {
        Injector injector = Guice.createInjector(new ExtensionFinder(Thread.currentThread().getContextClassLoader()));
        return injector.getInstance(getTestClass().getJavaClass());
    }
}
