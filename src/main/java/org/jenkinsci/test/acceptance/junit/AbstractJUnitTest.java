package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.ByFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

/**
 * Convenience base class to derive your plain-old JUnit tests from.
 *
 * <p>
 * It provides a number of convenience methods, and sets up the correct test runner.
 *
 * @author Kohsuke Kawaguchi
 */
public class AbstractJUnitTest extends Assert {
    @Rule
    public JenkinsAcceptanceTestRule env = new JenkinsAcceptanceTestRule();

    @Inject
    public WebDriver driver;

    /**
     * Obtains a resource in a wrapper.
     */
    public Resource resource(String path) {
        return new Resource(getClass().getResource(path));
    }

    public static final ByFactory by = new ByFactory();
}
