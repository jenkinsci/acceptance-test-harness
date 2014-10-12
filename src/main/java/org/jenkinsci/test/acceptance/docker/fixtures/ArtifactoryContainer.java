package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by eli on 10/6/14.
 */
@DockerFixture(id = "artifactory",ports = 8081)
public class ArtifactoryContainer extends DockerContainer {


    public URL getURL() throws MalformedURLException {
        return new URL("http://"+ipBound(8081)+":" + port(8081) + "/artifactory");
    }
}
