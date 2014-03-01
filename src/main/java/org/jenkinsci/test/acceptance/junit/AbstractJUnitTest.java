package org.jenkinsci.test.acceptance.junit;

import org.junit.Assert;

/**
 * @author Kohsuke Kawaguchi
 */
public class AbstractJUnitTest extends Assert {
    /**
     * Obtains a resource in a wrapper.
     */
    public Resource resource(String path) {
        return new Resource(getClass().getResource(path));
    }
}
