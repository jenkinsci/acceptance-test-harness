package org.jenkinsci.test.acceptance;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Indexed for runtime dicovery
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
@Indexed
public @interface Glue {
}
