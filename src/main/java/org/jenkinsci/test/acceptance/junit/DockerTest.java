package org.jenkinsci.test.acceptance.junit;

import java.lang.annotation.Inherited;

/**
 * Marker interface to identify a Docker test. Used to get categories working properly with {@link WithDocker}.
 *
 * @author Andrew Bayer
 */
public interface DockerTest {
    // marker interface
}
