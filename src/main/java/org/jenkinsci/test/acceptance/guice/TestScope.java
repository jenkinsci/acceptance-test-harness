package org.jenkinsci.test.acceptance.guice;

import com.google.inject.ScopeAnnotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that instances of this component are scoped to each test case.
 * <p>
 * {@link TestScope} is tied to a thread that executes a test, in anticipation of multi-threaded
 * concurrent test executions. See {@link World}
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
@ScopeAnnotation
public @interface TestScope {}
