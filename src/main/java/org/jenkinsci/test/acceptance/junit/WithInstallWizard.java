package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Enables the install wizard to run the test. This is only possible for LocalControllers. Otherwise the test is
 * skipped.
 * <p>
 * Note the Jenkins will not have form-element-path installed automatically preventing {@link org.jenkinsci.test.acceptance.po.Control}
 * and {@link org.jenkinsci.test.acceptance.po.PageArea} to use "path" based navigation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
@Documented
@RuleAnnotation(value = WithInstallWizard.RuleImpl.class, priority = -10) // Run before Jenkins startup
public @interface WithInstallWizard {
    
    class RuleImpl implements TestRule {
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
