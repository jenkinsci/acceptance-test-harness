package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.Extension;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Run test against existing Jenkins instance.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExistingJenkinsController extends JenkinsController {

    private URL url;

    public ExistingJenkinsController(URL url) {
        this.url = url;
    }

    @Override
    public void startNow() {
        // noop
    }

    @Override
    public void stopNow() {
        // noop
    }

    @Override
    public void populateJenkinsHome(File template, boolean clean) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void tearDown() {
    }

    @Extension
    public static class FactoryImpl implements JenkinsControllerFactory {
        @Override
        public String getId() {
            return "existing";
        }

        @Override
        public JenkinsController create() {
            String url = System.getenv("JENKINS_URL");
            if (url==null)  url = "http://localhost:8080/";

            try {
                return new ExistingJenkinsController(new URL(url));
            } catch (IOException e) {
                throw new AssertionError("Invalid URL: "+url,e);
            }
        }
    }
}
