package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.utils.process.CommandBuilder;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.Inject;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Indicates the native commands necessary to run tests.
 *
 * <p>
 * If any of these commands do not exist, the test gets skipped.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(Native.RuleImpl.class)
public @interface Native {
    String[] value();

    public class RuleImpl implements TestRule {
        @Inject
        Docker docker;
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
                    if (n==null)        return;
                    for (String cmd : n.value()) {
                        //!workaround for docker
                        if(cmd.contains("docker")){
                            if(!docker.isAvailable()){
                                throw new AssumptionViolatedException(cmd + " is needed for the test but doesn't exist in the system");
                            }

                        }
                        else if (new CommandBuilder("which",cmd).system()!=0) {
                            throw new AssumptionViolatedException(cmd + " is needed for the test but doesn't exist in the system");
                        }
                    }
                }
            };
        }
    }
}
