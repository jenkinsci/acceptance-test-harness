package org.jenkinsci.test.acceptance.po;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks {@link Job} subtypes.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexed
public @interface JobPageObject {
    /**
     * Value of the radio button in the new job page. This is normally the fully qualified model class name
     * in Jenkins.
     */
    String value();
}
