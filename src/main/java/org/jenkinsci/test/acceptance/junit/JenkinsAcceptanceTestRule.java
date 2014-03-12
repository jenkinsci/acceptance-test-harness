package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.World;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

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
            @Inject
            Jenkins jenkins;

            public void evaluate() throws Throwable {
                World world = World.get();
                world.startTestScope();
                Injector injector = world.getInjector();

                injector.injectMembers(target);
                injector.injectMembers(this);

                try {
                    // honor this annotation on a method, and if not try looking at the class
                    if (!installPlugins(method.getAnnotation(WithPlugins.class)))
                        installPlugins(target.getClass().getAnnotation(WithPlugins.class));

                    base.evaluate();
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
}
