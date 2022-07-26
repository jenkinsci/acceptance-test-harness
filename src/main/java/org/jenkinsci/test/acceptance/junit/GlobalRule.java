package org.jenkinsci.test.acceptance.junit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jvnet.hudson.annotation_indexer.Indexed;

/**
 * {@link TestRule} to be applied on all tests globally.
 *
 * Annotate {@link TestRule} to have it run for every test.
 *
 * @author ogondza
 * @see {@link RuleAnnotation} optional rule registration.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Indexed
public @interface GlobalRule {

    /**
     * Optional ordering among rules.
     *
     * Annotation with <tt>priority >= 0</tt> are guaranteed to be run after
     * Jenkins is up. Negative priorities are run before startup on best effort
     * basis. (It might not happen before for ExistingJenkinsController,
     * PooledJenkinsController and possibly others).
     *
     * Annotations that skips execution are encouraged to run before Jenkins is
     * booted up to save time. Note, that these implementations can not inject
     * Jenkins for obvious reasons.
     */
    int priority() default 0;
}
