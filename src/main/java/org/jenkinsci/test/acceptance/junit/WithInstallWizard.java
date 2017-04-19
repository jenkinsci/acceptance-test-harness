package org.jenkinsci.test.acceptance.junit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Inject;

/**
 * Enables the install wizard to run the test. This is only possible for LocalControllers. Otherwise the test is
 * skipped.
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Inherited
@Documented
@RuleAnnotation(value = WithInstallWizard.RuleImpl.class, priority = -10) // Run before Jenkins startup
public @interface WithInstallWizard {
    
    public class RuleImpl implements TestRule {
        @Inject
        JenkinsController controller;

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    enableWizard(d.getAnnotation(WithInstallWizard.class));
                    enableWizard(d.getTestClass().getAnnotation(WithInstallWizard.class));
                    base.evaluate();
                }

                private void enableWizard(WithInstallWizard n) {
                    if (n==null) return;
                    if (controller instanceof LocalController) {
                        ((LocalController) controller).setRunInstallWizard(true);
                    } else {
                        throw new AssumptionViolatedException("Testing the setup wizard is only supported if a LocalController is in use. Test will be skipped.");
                    }
                }
            };
        }
    }
}
