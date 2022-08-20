package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;

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
 * Minimal Jenkins version required to run the test.
 *
 * Declares that running the test with older version is pointless, typically because of missing feature.
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

    class RuleImpl implements TestRule {
        @Inject
        Injector injector;

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
                        VersionNumber actual = injector.getInstance(Jenkins.class).getVersion();
                        VersionNumber expected = new VersionNumber(s.value());
                        System.out.printf("Version check: actual=%s, expected=%s\n",actual,expected);
                        Assume.assumeTrue("Requires "+s.value(), actual.compareTo(expected)>=0);
                    }
                }
            };
        }
    }
}
