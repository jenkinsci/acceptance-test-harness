package org.jenkinsci.test.acceptance.junit;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.utils.process.CommandBuilder;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Indicates the native commands necessary to run tests.
 *
 * <p>
 * If any of these commands do not exist, the test gets skipped.
 *
 * @author Kohsuke Kawaguchi
 * @deprecated Refactor to use containers for any kind of expected setup
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
@Deprecated
@RuleAnnotation(Native.RuleImpl.class)
public @interface Native {
    String[] value();

    class RuleImpl implements TestRule {
        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    verifyNativeCommandPresent(d.getAnnotation(Native.class));
                    verifyNativeCommandPresent(d.getTestClass().getAnnotation(Native.class));

                    base.evaluate();
                }

                private void verifyNativeCommandPresent(Native n) throws IOException, InterruptedException {
                    if (n == null) {
                        return;
                    }
                    for (String cmd : n.value()) {
                        if (SystemUtils.IS_OS_WINDOWS) {
                            if (new CommandBuilder("where", cmd).system() != 0) {
                                throw new AssumptionViolatedException(
                                        cmd + " is needed for the test but doesn't exist in the system");
                            }
                        } else {
                            if (new CommandBuilder("which", cmd).system() != 0) {
                                throw new AssumptionViolatedException(
                                        cmd + " is needed for the test but doesn't exist in the system");
                            }
                        }
                    }
                }
            };
        }
    }
}
