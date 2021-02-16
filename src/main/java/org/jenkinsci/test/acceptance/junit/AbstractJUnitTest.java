package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.utils.SupportBundleRequest;
import org.junit.After;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import java.io.File;
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
    public JenkinsAcceptanceTestRule rules = new JenkinsAcceptanceTestRule();

    /**
     * Jenkins under test.
     */
    @Inject
    public Jenkins jenkins;

    @Inject
    private FailureDiagnostics diagnostics;

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
    public void captureSupportBundle() {
        File file = diagnostics.touch("support-bundle.zip");
        jenkins.generateSupportBundle(SupportBundleRequest.builder().includeDefaultComponents().setOutputFile(file).build());
    }
}
