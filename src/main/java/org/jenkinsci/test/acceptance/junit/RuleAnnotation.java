package org.jenkinsci.test.acceptance.junit;

import org.junit.rules.TestRule;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Meta-annotation for annotations that introduces a {@link TestRule} for test.
 *
 * <p>
 * This allows annotations on test class/method to add additional setup/shutdown behaviours.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@Documented
public @interface RuleAnnotation {
    /**
     * The rule class that defines the setup/shutdown behaviour.
     *
     * The instance is obtained through Guice.
     */
    Class<? extends TestRule> value();

    /** Optional ordering among rules. */
    int priority() default 0;
}
