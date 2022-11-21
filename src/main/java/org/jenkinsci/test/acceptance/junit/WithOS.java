package org.jenkinsci.test.acceptance.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;

import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Inject;

/**
 * 
 * Indicates that the test and Jenkins instance must be running 
 * on one of the operating systems provided. If this condition is not
 * met, this test will be skipped.
 * 
 * <p>
 * This check can only be <strong>trusted to do its job correctly</strong> when tests 
 * are being run with a LocalController. When using a different controller, 
 * the machine running Jenkins can be different from the one running the tests so
 * the test may pass or fail when it should not. If tests are not being run with a 
 * LocalController, the test will be skipped.
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(value = WithOS.RuleImpl.class, priority = -10)
public @interface WithOS {

    enum OS {
        WINDOWS,
        LINUX,
        MAC
    }
    
    OS[] os();

    class RuleImpl implements TestRule {
        @Inject JenkinsController controller;
        
        @Override
        public Statement apply(final Statement base, final Description d) {
           return new Statement() {
               
            @Override
            public void evaluate() throws Throwable {
                Class<?> testSuite = d.getTestClass();
                check(d.getAnnotation(WithOS.class), testSuite);
                check(testSuite.getAnnotation(WithOS.class), testSuite);

                base.evaluate();
            }

            private void check(WithOS withos, Class<?> testCase) {
                if (withos == null) return;

                // Checks performed in this annotation only makes sense if Jenkins and Tests are being executed in the same machine
                if (!(controller instanceof LocalController)) {
                    throw new AssumptionViolatedException("Test skipped. WithOS should be used with a local controller, otherwise it cannot be fully trusted to work as expected.");
                }
                
                String errorMsg = "Test and Jenkins instance must be running on any of the following operating systems: " + Arrays.toString(withos.os());
                if (!Arrays.asList(withos.os()).contains(currentOS())) {
                    throw new AssumptionViolatedException(errorMsg);
                }
            }
            
            private OS currentOS() {
                if (SystemUtils.IS_OS_LINUX) {
                    return OS.LINUX;
                }
                if (SystemUtils.IS_OS_WINDOWS) {
                    return OS.WINDOWS;
                }
                if (SystemUtils.IS_OS_MAC) {
                    return OS.MAC;
                }
                throw new RuntimeException("Unrecognized OS");
            }
        };
        }
    }
}
