package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Abstract base class factoring out common code for test classes using the {@link JenkinsAcceptanceTestRule}.
 *
 * @author Andres Rodriguez
 */
public abstract class AbstractATHTest extends CapybaraPortingLayerImpl {

    protected AbstractATHTest() {
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
}
