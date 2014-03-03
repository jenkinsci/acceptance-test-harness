package org.jenkinsci.test.acceptance.guice;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Indicates that instances of this component are scoped to each test case.
 *
 * {@link TestScope} is tied to a thread that executes a test, in anticipation of multi-threaded
 * concurrent test executions. See {@link World#testScopeObjects}
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Inherited
@Documented
public @interface TestScope {
}
