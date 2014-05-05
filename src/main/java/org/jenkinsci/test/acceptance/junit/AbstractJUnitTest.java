package org.jenkinsci.test.acceptance.junit;

import javax.inject.Inject;
import java.net.URL;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;

/**
 * Convenience base class to derive your plain-old JUnit tests from.
 * <p/>
 * <p/>
 * It provides a number of convenience methods, and sets up the correct test runner.
 *
 * @author Kohsuke Kawaguchi
 */
public class AbstractJUnitTest extends CapybaraPortingLayerImpl {
    @Rule
    public JenkinsAcceptanceTestRule env = new JenkinsAcceptanceTestRule();

    /**
     * Jenkins under test.
     */
    @Inject
    public Jenkins jenkins;

    /**
     * This field receives a valid web driver object you can use to talk to Jenkins.
     */
    @Inject
    public WebDriver driver;

    public AbstractJUnitTest() {
        super(null);
    }

    /**
     * Obtains a resource in a wrapper.
     */
    public Resource resource(String path) {
        final URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new AssertionError("No such resource " + path + " for " + getClass().getName());
        }
        return new Resource(resource);
    }
}
