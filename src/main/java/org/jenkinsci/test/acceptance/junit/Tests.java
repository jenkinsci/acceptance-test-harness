package org.jenkinsci.test.acceptance.junit;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Class providing miscellaneous support methods for tests.
 *
 * @author Andres Rodriguez
 */
public final class Tests {
    /** Not instantiable. */
    private Tests() {
        throw new AssertionError("Not instantiable");
    }

    /**
     * @return finds an unused, available port on the test machine
     */
    public static int findAvailablePort() {
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
