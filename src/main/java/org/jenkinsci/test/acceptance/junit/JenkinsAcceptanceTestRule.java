package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.World;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.utils.process.CommandBuilder;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

/**
 * Runs Guice container that houses {@link JenkinsController}, {@link WebDriver}, and so on.
 *
 * <p>
 * Add this rule to your Unit test class if you want to leverage this harness.
 *
 * <p>
 * This is the glue that connects JUnit to the logic of the test harness (but to support other test harnesses
 * like cucumber, we are trying to minimize what to put in here.)
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsAcceptanceTestRule implements MethodRule {
    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {
            @Inject Jenkins jenkins;
            @Inject JenkinsController controller;

            public void evaluate() throws Throwable {
                World world = World.get();
                Injector injector = world.getInjector();

                world.startTestScope();

                injector.injectMembers(target);
                injector.injectMembers(this);

                try {
                    verifyNativeCommandPresent(method.getAnnotation(Native.class));
                    verifyNativeCommandPresent(target.getClass().getAnnotation(Native.class));

                    // honor this annotation on a method, and if not try looking at the class
                    if (!installPlugins(method.getAnnotation(WithPlugins.class)))
                        installPlugins(target.getClass().getAnnotation(WithPlugins.class));

                    base.evaluate();
                } catch (Exception|AssertionError e) { // Errors and failures
                    controller.diagnose(e);
                    throw e;
                } finally {
                    world.endTestScope();
                }
            }

            private boolean installPlugins(WithPlugins wp) {
                if (wp!=null)
                    jenkins.getPluginManager().installPlugin(wp.value());
                return wp!=null;
            }
        };
    }

    private void verifyNativeCommandPresent(Native n) throws IOException, InterruptedException {
        for (String cmd : n.value()) {
            if (new CommandBuilder("which",cmd).system()!=0) {
                throw new AssumptionViolatedException(cmd + " is needed for the test but doesn't exist in the system");
            }
        }
    }
}
