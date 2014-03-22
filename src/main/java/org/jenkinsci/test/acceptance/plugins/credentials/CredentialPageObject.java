package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks subtype of {@link Credential}.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexed
public @interface CredentialPageObject {
    /**
     * Display name.
     */
    String value();
}
