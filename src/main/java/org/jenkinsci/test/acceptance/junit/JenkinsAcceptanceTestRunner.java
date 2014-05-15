package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.guice.World;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

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
        return World.get().getInjector().getInstance(getTestClass().getJavaClass());
    }

    protected Statement methodBlock(final FrameworkMethod method) {
        final Statement base = super.methodBlock(method);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                World world = World.get();
                world.startTestScope(getTestName());
                try {
                    base.evaluate();
                } finally {
                    world.endTestScope();
                }
            }

            private String getTestName() {
                return method.getMethod().getDeclaringClass().getName()+"."+method.getName();
            }
        };
    }

}
