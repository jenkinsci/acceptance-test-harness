package org.jenkinsci.test.acceptance.po;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the {@link BuildStep} subtype to associate its display name in the UI
 * to the {@link PageArea} implementation.
 *
 * @author Kohsuke Kawaguchi
 * @see Job#addBuildStep(Class)
 * @see Job#addPublisher(Class)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexed
public @interface BuildStepPageObject {
    /**
     * Display name of this build step.
     */
    String value();
}
