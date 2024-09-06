package org.jenkinsci.test.acceptance.junit;

import jakarta.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.recorder.SupportBundle;
import org.jenkinsci.test.acceptance.utils.SupportBundleRequest;
import org.junit.After;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;

/**
 * Convenience base class to derive your plain-old JUnit tests from.
 * <p>
 * It provides a number of convenience methods, and sets up the correct test runner.
 *
 * @author Kohsuke Kawaguchi
 */
public class AbstractJUnitTest extends CapybaraPortingLayerImpl {
    private static final Logger LOGGER = Logger.getLogger(AbstractJUnitTest.class.getName());

    @Rule
    public JenkinsAcceptanceTestRule rules = new JenkinsAcceptanceTestRule();

    /**
     * Jenkins under test.
     */
    @Inject
    public Jenkins jenkins;

    @Inject
    private FailureDiagnostics diagnostics;

    @Rule(order = 0) // enclosed by JenkinsAcceptanceTestRule so that we have a valid Jenkins + webdriver
    public SupportBundle supportBundle = new SupportBundle();

    /**
     * This field receives a valid web driver object you can use to talk to Jenkins.
     */
    @Inject
    public WebDriver driver;

    public AbstractJUnitTest() {
        super(null);
    }

    /**
     * @return finds an unused, available port on the test machine
     */
    public int findAvailablePort() {
        // use port 65000 as fallback (but maybe there is something running)
        int port = 65000;
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    protected void interrupt() {
        throw new RuntimeException("INTERACTIVE debugging");
    }

    @After
    public void injectSpec() {
        supportBundle.addSpec(
                jenkins,
                SupportBundleRequest.builder()
                        .includeDefaultComponents()
                        .setOutputFile(diagnostics.touch("support-bundle.zip"))
                        .build());
    }
}
