package org.jenkinsci.test.acceptance.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;

import org.apache.commons.lang.SystemUtils;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

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
 * the test may pass or fail when it should not.
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(value = WithOS.RuleImpl.class, priority = -10)
public @interface WithOS {

    public enum OS {
        WINDOWS,
        LINUX,
        MAC
    }
    
    OS[] os();

    public class RuleImpl implements TestRule {
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
                
                String errorMsg = "Test and Jenkins instance must be running on any of the following operating systems: " + Arrays.toString(withos.os());
                for (OS _os : withos.os()) {
                    switch (_os) {
                    case LINUX:
                        if (!SystemUtils.IS_OS_LINUX) {
                            throw new AssumptionViolatedException(errorMsg);
                        }
                        break;
                    case WINDOWS:
                        if (!SystemUtils.IS_OS_WINDOWS) {
                            throw new AssumptionViolatedException(errorMsg);
                        }
                        break;
                    case MAC:
                        if (!SystemUtils.IS_OS_MAC) {
                            throw new AssumptionViolatedException(errorMsg);
                        }
                        break;
                    default:
                        break;
                    }
                }
                
            }
        };
        }
    }
}
