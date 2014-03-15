package org.jenkinsci.test.acceptance.junit;

import java.net.URL;

import org.jenkinsci.test.acceptance.ByFactory;
import org.jenkinsci.test.acceptance.po.Jenkins;
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

    /**
     * This field receives a valid web driver object you can use to talk to Jenkins.
     */
    @Inject
    public WebDriver driver;

    /**
     * Obtains a resource in a wrapper.
     */
    public Resource resource(String path) {
        final URL resource = getClass().getResource(path);
        if (resource == null) throw new AssertionError("No such resource " + path + " for " + getClass().getName());
        return new Resource(resource);
    }

    public static final ByFactory by = new ByFactory();
}
