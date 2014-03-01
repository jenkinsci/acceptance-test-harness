package org.jenkinsci.test.acceptance.po;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates {@link Parameter} subtype.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexed
public @interface ParameterPageObject {
    /**
     * Display name of the parameter page object. This is normally the fully qualified model class name
     * in Jenkins.
     */
    String value();
}
