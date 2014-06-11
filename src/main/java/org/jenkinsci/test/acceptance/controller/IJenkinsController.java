package org.jenkinsci.test.acceptance.controller;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

/**
 * Remoting interface for {@link JenkinsController}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface IJenkinsController extends Closeable {
    URL getUrl();
    void start() throws IOException;
    void stop() throws IOException;
}
