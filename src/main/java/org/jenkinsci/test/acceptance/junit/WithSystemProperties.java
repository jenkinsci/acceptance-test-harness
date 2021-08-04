package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.WinstoneController;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 
 * Indicates that the test and Jenkins instance will run with the specified
 * system properties. These properties are set as an array of pairs <i>property=vale</i>.
 * 
 * <p>
 * The system properties can be set only when the test are being run with a {@link WinstoneController}.
 * This annotation is a way to extend JENKINS_JAVA_OPTS and JENKINS_OPTS at test level
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(value = WithSystemProperties.RuleImpl.class, priority = -10)
public @interface WithSystemProperties {

    /**
     * See {@link PluginSpec} for the syntax.
     */
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
                        throw new AssumptionViolatedException("Test skipped. WithSystemProperties should be used with a local controller.");
                    }

                    Class<?> testSuite = d.getTestClass();
                    final String[] fromClass = getProperties(d.getAnnotation(WithSystemProperties.class));
                    if (fromClass != null) {
                        for (String property : fromClass) {
                            ((WinstoneController) controller).addSystemProperty(property);
                        }
                    }
                    final String[] fromMethod = getProperties(testSuite.getAnnotation(WithSystemProperties.class));
                    if (fromMethod != null) {
                        for (String property : fromMethod) {
                            ((WinstoneController) controller).addSystemProperty(property);
                        }
                    }

                    base.evaluate();
                }
            };
        }

        // Visible for testing
        static String[] getProperties(WithSystemProperties withSystemProperties) {
            if (withSystemProperties == null) {
                return null;
            }

            String[] properties = withSystemProperties.value();
            if (properties == null || properties.length == 0) {
                return null;
            }

            List<String> systemProperties = new ArrayList<>();
            for (String property : properties) {
                if (StringUtils.isNotBlank(property)) {
                    if (!property.startsWith("-D")) {
                        property = "-D" + property;
                    }
                    systemProperties.add(property);
                }
            }
            return systemProperties.toArray(new String[0]);
        }
    }
}
