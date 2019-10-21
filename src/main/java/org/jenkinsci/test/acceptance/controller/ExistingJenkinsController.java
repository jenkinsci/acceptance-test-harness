package org.jenkinsci.test.acceptance.controller;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.Injector;

/**
 * Run test against existing Jenkins instance.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExistingJenkinsController extends JenkinsController {
    private final URL uploadUrl;
    private final URL url;

    public ExistingJenkinsController(Injector i, String url) {
        super(i);
        try {
            this.url = new URL(url);
            this.uploadUrl = new URL(url + "/pluginManager/api/xml");
        } catch (IOException e) {
            throw new AssertionError("Invalid URL: "+url,e);
        }
    }

    @Override
    public void startNow() {
        verifyThatFormPathElementPluginIsInstalled();
    }

    private void verifyThatFormPathElementPluginIsInstalled() {
        try (CloseableHttpClient httpclient = new DefaultHttpClient()){
            HttpPost post = new HttpPost(uploadUrl.toExternalForm());

            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("depth", "1"));
            parameters.add(new BasicNameValuePair("xpath", "/*/*/shortName|/*/*/version"));
            parameters.add(new BasicNameValuePair("wrapper", "plugins"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
            post.setEntity(entity);

            HttpResponse response = httpclient.execute(post);
            HttpEntity resEntity = response.getEntity();
            String responseBody = EntityUtils.toString(resEntity);

            if (!responseBody.contains("form-element-path")) {
                failTestSuite(response.toString());
            }
        }
        catch (IOException exception) {
            throw new AssertionError("Can't check if form-element-path plugin is installed", exception);
        } 
    }

    private void failTestSuite(final String errorMessage) {
        throw new RuntimeException("Test suite requires in pre-installed Jenkins plugin https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin\n" + errorMessage);
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
            if (url==null)  url = "http://localhost:8080/";

            return new ExistingJenkinsController(i, url);
        }
    }
}
