package org.jenkinsci.test.acceptance.junit;

import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * Convenience base class to derive your plain-old JUnit tests from.
 *
 * <p>
 * It provides a number of convenience methods, and sets up the correct test runner.
 *
 * @author Kohsuke Kawaguchi
 */
@RunWith(JenkinsAcceptanceTestRunner.class)
public class AbstractJUnitTest extends Assert {
    /**
     * Obtains a resource in a wrapper.
     */
    public Resource resource(String path) {
        return new Resource(getClass().getResource(path));
    }
}
