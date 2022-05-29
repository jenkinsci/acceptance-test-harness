package org.jenkinsci.test.acceptance.controller;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.Injector;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * Run test against existing Jenkins instance.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExistingJenkinsController extends JenkinsController {
    private final URL url;
    private boolean skipCheck;

    /**
     * Credentials before any {@link org.jenkinsci.test.acceptance.po.SecurityRealm} is applied by a test.
     */
    @CheckForNull
    private Credentials initialCredentials;

    public ExistingJenkinsController(Injector i, String url) {
        this(i, url, null, false);
    }

    public ExistingJenkinsController(Injector i, String url, @CheckForNull Credentials initialCredentials, boolean skipCheck) {
        super(i);
        try {
            this.url = new URL(url);
            this.initialCredentials = initialCredentials;
            this.skipCheck = skipCheck;
        } catch (IOException e) {
            throw new AssertionError("Invalid URL: "+url,e);
        }
    }

    @Override
    public void startNow() {
    }

    @Override
    public void stopNow() {
        // noop
    }

    @Override
    public void populateJenkinsHome(byte[] template, boolean clean) throws IOException {
        // TODO use CLI or /script or something to unpack this
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @CheckForNull
    @Override
    public Credentials getInitialCredentials() {
        return initialCredentials;
    }

    @Override
    public void tearDown() {
    }

    @Extension
    public static class FactoryImpl implements JenkinsControllerFactory {
        @Inject Injector i;

        @Override
        public String getId() {
            return "existing";
        }

        @Override
        public JenkinsController create() {
            String url = System.getenv("JENKINS_URL");
            String username = System.getenv("JENKINS_USERNAME");
            String password = System.getenv("JENKINS_PASSWORD");
            UsernamePasswordCredentials initialCredentials = null;
            if (username != null && password != null) {
                initialCredentials = new UsernamePasswordCredentials(username, password);
            }
            if (url==null)  url = "http://localhost:8080/";

            return new ExistingJenkinsController(i, url, initialCredentials, false);
        }
    }
}
