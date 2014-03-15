package org.jenkinsci.test.acceptance.po;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Marks {@link View} subtypes.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
@Indexed
public @interface ViewPageObject {
    /**
     * Value of the radio button in the new view page. This is normally the fully qualified model class name
     * in Jenkins.
     */
    String value();
}