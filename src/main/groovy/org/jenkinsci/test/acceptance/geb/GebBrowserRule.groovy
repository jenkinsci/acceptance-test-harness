package org.jenkinsci.test.acceptance.geb

import org.jenkinsci.test.acceptance.guice.World
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.openqa.selenium.WebDriver

/**
 * jUnit {@link org.junit.Rule} for injecting the webdriver into all geb specs.
 *
 * @author christian.fritz
 */
class GebBrowserRule implements MethodRule {
    /**
     * Modifies the method-running {@link Statement} to implement an additional
     * test-running rule.
     *
     * @param base The {@link Statement} to be modified
     * @param method The method to be run
     * @param target The object on with the method will be run.
     * @return a new statement, which may be the same as {@code base},
     *         a wrapper around {@code base}, or a completely new Statement.
     */
    @Override
    Statement apply(Statement base, FrameworkMethod method, Object target) {
        new Statement() {
            @Override
            void evaluate() throws Throwable {
                target?.browser?.config?.driver = World.get().getInjector().getInstance(WebDriver.class);
                base.evaluate();
            }
        }
    }
}
