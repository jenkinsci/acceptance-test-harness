package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import java.io.IOException;
import java.net.ServerSocket;

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
    public JenkinsAcceptanceTestRule rules = new JenkinsAcceptanceTestRule(this);

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
}
