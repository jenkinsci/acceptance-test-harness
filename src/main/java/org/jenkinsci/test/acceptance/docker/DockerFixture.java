package org.jenkinsci.test.acceptance.docker;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotates {@link DockerContainer} subtype that exposes fixture-specific methods.
 *
 * @author Kohsuke Kawaguchi
 */
@Target(TYPE)
@Retention(RUNTIME)
@Indexed
@Inherited
public @interface DockerFixture {
    /**
     * Unique ID of this fixture. Used from cucumber, etc. to find this annotation.
     */
    String id();

    /**
     * Ports that are exposed from this fixture.
     */
    int[] ports() default {};
}
