package org.jenkinsci.test.acceptance.junit;

import java.lang.annotation.Documented;

/**
 * Indicates bug ID that the test is associated with.
 *
 * @author Oliver Gondza
 */
@Documented
public @interface Bug {
    String value();
}
