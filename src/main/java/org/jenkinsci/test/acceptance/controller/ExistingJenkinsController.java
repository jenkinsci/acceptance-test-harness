package org.jenkinsci.test.acceptance.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.Inject;

/**
 * Run test against existing Jenkins instance.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExistingJenkinsController extends JenkinsController {
    private final URL uploadUrl;
    private final URL url;
    private final File formElementsPathFile;

    public ExistingJenkinsController(String url, final File formElementsPathFile) {
        try {
            this.url = new URL(url);
            this.uploadUrl = new URL(url + "/pluginManager/api/xml");
            this.formElementsPathFile = formElementsPathFile;
        } catch (IOException e) {
            throw new AssertionError("Invalid URL: "+url,e);
        }
    }

    @Override
    public void startNow() {
        verifyThatFormPathElementPluginIsInstalled();
    }

    private void verifyThatFormPathElementPluginIsInstalled() {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost post = new HttpPost(uploadUrl.toExternalForm());

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("depth", "1"));
            formparams.add(new BasicNameValuePair("xpath", "/*/*/shortName|/*/*/version"));
            formparams.add(new BasicNameValuePair("wrapper", "plugins"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(entity);

            HttpResponse response = httpclient.execute(post);
            HttpEntity resEntity = response.getEntity();
            String responseBody = EntityUtils.toString(resEntity);

            if (!responseBody.contains("form-element-path")) {
                failTestSuite();
            }
        }
        catch (IOException exception) {
            throw new AssertionError("Can't check if form-element-path plugin is installed", exception);
        }
    }

    private void failTestSuite() {
        throw new RuntimeException("Test suite requires in pre-installed Jenkins plugin https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin");
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
        @Inject
        private RepositorySystem repositorySystem;
        @Inject
        private RepositorySystemSession repositorySystemSession;

        @Override
        public String getId() {
            return "existing";
        }

        @Override
        public JenkinsController create() {
            String url = System.getenv("JENKINS_URL");
            if (url==null)  url = "http://localhost:8080/";

            return new ExistingJenkinsController(url, getFormElementsPathFile());
        }

        /**
         * Returns the path to the form elements plug-in. Uses the Maven repository to obtain the plugin.
         *
         * @return the path to the form elements plug-in
         */
        protected File getFormElementsPathFile() {
            try {
                ArtifactResult resolvedArtifact = repositorySystem.resolveArtifact(repositorySystemSession,
                        new ArtifactRequest(new DefaultArtifact("org.jenkins-ci.plugins", "form-element-path", "hpi", "1.4"),
                                Arrays.asList(new RemoteRepository.Builder("repo.jenkins-ci.org", "default", "http://repo.jenkins-ci.org/public/").build()),
                                null));
                return resolvedArtifact.getArtifact().getFile();
            }
            catch (ArtifactResolutionException e) {
                throw new RuntimeException("Could not resolve form-element-path.hpi from Maven repository repo.jenkins-ci.org.", e);
            }
        }
    }
}
