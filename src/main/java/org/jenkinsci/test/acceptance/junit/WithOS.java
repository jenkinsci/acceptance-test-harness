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
 * Indicates that the test must be run on a machine running 
 * one of the operating systems provided
 * 
 * If the test is not running on na accepted operating system 
 * the test will be skipped before Jenkins boots up
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
                Class<?> testCase = d.getTestClass();
                check(d.getAnnotation(WithOS.class), testCase);
                check(testCase.getAnnotation(WithOS.class), testCase);

                base.evaluate();
            }

            private void check(WithOS withos, Class<?> testCase) {
                if (withos == null) return;
                
                String errorMsg = "Test must be run on a machine running any of the following operating systems: " + Arrays.toString(withos.os());
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
