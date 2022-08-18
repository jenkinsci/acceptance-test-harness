package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.WinstoneController;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 
 * Indicates that the test and Jenkins instance will run with the specified
 * java options. These options are set as an array of pairs <i>property=vale</i>.
 * 
 * <p>
 * The options can be set only when the test are being run with a {@link WinstoneController}.
 * This annotation is a way to extend JENKINS_JAVA_OPTS and JENKINS_OPTS at test level
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(value = WithJavaOptions.RuleImpl.class, priority = -10)
public @interface WithJavaOptions {

    String[] value();

    public class RuleImpl implements TestRule {
        @Inject
        JenkinsController controller;

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    if (!(controller instanceof WinstoneController)) {
                        throw new AssumptionViolatedException("Test skipped. WithSystemProperties should be used with a winstone controller.");
                    }

                    Class<?> testSuite = d.getTestClass();
                    final String[] fromClass = getOptions(d.getAnnotation(WithJavaOptions.class));
                    if (fromClass != null) {
                        for (String property : fromClass) {
                            ((WinstoneController) controller).addJavaOpt(property);
                        }
                    }
                    final String[] fromMethod = getOptions(testSuite.getAnnotation(WithJavaOptions.class));
                    if (fromMethod != null) {
                        for (String property : fromMethod) {
                            ((WinstoneController) controller).addJavaOpt(property);
                        }
                    }

                    base.evaluate();
                }
            };
        }

        // Visible for testing
        static String[] getOptions(WithJavaOptions withJavaOptions) {
            if (withJavaOptions == null) {
                return null;
            }

            String[] properties = withJavaOptions.value();
            if (properties == null || properties.length == 0) {
                return null;
            }

            return properties;
        }
    }
}
