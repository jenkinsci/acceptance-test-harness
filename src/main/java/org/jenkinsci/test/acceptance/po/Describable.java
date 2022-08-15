package org.jenkinsci.test.acceptance.po;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jvnet.hudson.annotation_indexer.Indexed;

/**
 * Annotation used to register implementations to be discovered automatically.
 * <p>
 * Note that different services creating an instances have different conventions concerning both the values of this
 * annotation as well as the class interface. In some cases, descriptions are visual labels used in UI, but it can as
 * well be an internal identifier such as Jenkins class name. Compare {@link Describable} annotations for {@link
 * MatrixProject} and {@link ShellBuildStep}. Unique constructor signature is often required for implementations of the
 * same abstraction.
 * <p>
 * The details should be documented in particular superclass, such as {@link Job} or {@link BuildStep}.
 *
 * @author Kohsuke Kawaguchi
 * @see CapybaraPortingLayerImpl#findCaption(Class, CapybaraPortingLayerImpl.Finder)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexed
public @interface Describable {
    /**
     * Descriptions.
     *
     * The annotation accepts several values as possible alternatives. First that exists will be used.
     */
    String[] value();
}
