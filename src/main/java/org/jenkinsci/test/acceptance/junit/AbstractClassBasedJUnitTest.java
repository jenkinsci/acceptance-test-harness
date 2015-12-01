package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.ClassRule;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Convenience base class to derive your plain-old JUnit tests from when using {@link JenkinsAcceptanceTestRule} as a {@link ClassRule}.
 *
 * @author Andres Rodriguez
 */
public class AbstractClassBasedJUnitTest extends AbstractATHTest {

    @ClassRule
    public static JenkinsAcceptanceTestRule rules = JenkinsAcceptanceTestRule.classRule();

    /**
     * Jenkins under test.
     */
    @Inject
    public static Jenkins jenkins;

    /**
     * This field receives a valid web driver object you can use to talk to Jenkins.
     */
    @Inject
    public static WebDriver driver;

    public AbstractClassBasedJUnitTest() {
    }
}
