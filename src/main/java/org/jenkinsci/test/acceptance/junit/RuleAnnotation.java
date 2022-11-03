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
     * <p>
     * The instance is obtained through Guice.
     */
    Class<? extends TestRule> value();

    /**
     * Optional ordering among rules.
     * <p>
     * Annotation with {@code priority >= 0} are guaranteed to be run after
     * Jenkins is up. Negative priorities are run before startup on best effort
     * basis. (It might not happen before for ExistingJenkinsController,
     * PooledJenkinsController and possibly others).
     * <p>
     * Annotations that skips execution are encouraged to run before Jenkins is
     * booted up to save time. Note, that these implementations can not inject
     * Jenkins for obvious reasons.
     */
    int priority() default 0;
}
