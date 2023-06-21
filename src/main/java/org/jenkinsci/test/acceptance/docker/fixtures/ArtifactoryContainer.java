package org.jenkinsci.test.acceptance.docker.fixtures;

import java.net.MalformedURLException;
import java.net.URL;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Runs Artifactory OSS container
 */
@DockerFixture(id = "artifactory", ports = 8081)
public class ArtifactoryContainer extends DockerContainer {

    public URL getURL() {
        try {
            return new URL("http://" + ipBound(8081) + ":" + port(8081) + "/artifactory");
        } catch (MalformedURLException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * Rest Api to verify Artifactory is up and running
     */
    public URL getPingURL() throws MalformedURLException {
        return new URL("http://" + ipBound(8081) + ":" + port(8081) + "/artifactory/api/system/ping");
    }
}
