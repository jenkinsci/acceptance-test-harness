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
    /**
     * Populates the Jenkins Home with the specified ZIP template.
     * Jenkins will <em>not</em> be restarted, so if the content would require a restart you have to do this yourself.
     * @param template The template (ZIP format).
     * @param clean    if {@code true} then the home will be wiped clean before the template is applied. If false then
     *                 the template will simply overwrite the existing (if any) home.
     */
    void populateJenkinsHome(byte[] template, boolean clean) throws IOException;
}
