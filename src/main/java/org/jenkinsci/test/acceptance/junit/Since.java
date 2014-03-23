package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Version number of Jenkins that the test applies to.
 *
 * @author Oliver Gondza
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(Since.RuleImpl.class)
public @interface Since {
    String value();

    public class RuleImpl implements TestRule {
        @Inject
        Jenkins jenkins;

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    check(d.getAnnotation(Since.class));
                    check(d.getTestClass().getAnnotation(Since.class));

                    base.evaluate();
                }

                private void check(Since s) {
                    if (s!=null) {
                        Assume.assumeTrue("Requires "+s.value(), jenkins.getVersion().compareTo(new VersionNumber(s.value()))>=0);
                    }
                }
            };
        }
    }
}
