package org.jenkinsci.test.acceptance.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Indicates that a test requires the presence of the specified plugins.
 *
 * <p>
 * When running tests, this annotation triggers {@link JenkinsAcceptanceTestRule}
 * to install all the plugins.
 *
 * We also want to use this to filter tests, especially for non-destructive tests.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited @Documented
public @interface WithPlugins {
    String[] value();
}
