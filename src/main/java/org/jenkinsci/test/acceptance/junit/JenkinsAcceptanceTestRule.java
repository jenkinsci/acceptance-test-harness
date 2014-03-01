package org.jenkinsci.test.acceptance.junit;

import com.cloudbees.sdk.extensibility.ExtensionFinder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

/**
 * Runs Guice container that houses {@link JenkinsController}, {@link WebDriver}, and so on.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsAcceptanceTestRule implements MethodRule {
    @Override
    public Statement apply(final Statement base, FrameworkMethod method, final Object target) {
        return new Statement() {
            @Inject(optional=true)
            WebDriver driver;

            public void evaluate() throws Throwable {
                Injector injector = Guice.createInjector(new ExtensionFinder(Thread.currentThread().getContextClassLoader()));
                injector.injectMembers(target);
                injector.injectMembers(this);

                try {
                    base.evaluate();
                } finally {
                    if (driver!=null)
                        driver.close();
                }
            }
        };
    }
}
