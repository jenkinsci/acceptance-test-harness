package org.jenkinsci.test.acceptance.junit;

import org.junit.rules.TestRule;

/**
 * Meta-annotation for annotations that introduces a {@link TestRule} for test.
 *
 * <p>
 * This allows annotations on test class/method to add additional setup/shutdown behaviours.
 *
 * @author Kohsuke Kawaguchi
 */
public @interface RuleAnnotation {
    /**
     * The rule class that defines the setup/shutdown behaviour.
     *
     * The instance is obtained through Guice.
     */
    Class<? extends TestRule> value();
}
