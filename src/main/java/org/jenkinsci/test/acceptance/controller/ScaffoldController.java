package org.jenkinsci.test.acceptance.controller;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Scaffolding to run it against Jenkins that is run outside.
 *
 * @author Kohsuke Kawaguchi
 */
public class ScaffoldController extends JenkinsController {

    private URL url;

    public ScaffoldController() throws IOException {
        super();
        this.url = new URL("http://localhost:8080/");
    }

    @Override
    public void start() {

    }

    @Override
    public void startNow() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void stopNow() {

    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void diagnose() {

    }

    @Override
    public void tearDown() {

    }
}
