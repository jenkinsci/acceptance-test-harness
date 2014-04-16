package org.jenkinsci.test.acceptance.po;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexed
public @interface Describable {
    /**
     * Display name of this build step. Corresponds to {@code BuildStepDescriptor.getDisplayName()} on the server.
     */
    String[] value();
}
