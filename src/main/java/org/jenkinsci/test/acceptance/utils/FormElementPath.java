package org.jenkinsci.test.acceptance.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Ensures form-element-path plugin is running, and if not, installs it using the rest api to upload a plugin
 */
public class FormElementPath {
    private static final Logger LOGGER = Logger.getLogger(FormElementPath.class.getName());

    private Credentials credentials;

    @Inject
    @Named("form-element-path.hpi")
    private File formElementPathPlugin;

    @Inject
    private AccessTokenGenerator accessToken;

    public void ensure(@Nonnull URL url, @CheckForNull Credentials credentials) {
        try {
            if (credentials != null) {
                this.credentials = accessToken.generate(url, credentials);
            }
            if (!isFormPathElementPluginInstalled(url)) {
                LOGGER.info("Installing form-element-path plugin from " + formElementPathPlugin);
                uploadPlugin(url, credentials, formElementPathPlugin);
            }
        } catch (IOException e) {
            throw new AssertionError("Can't check if form-element-path plugin is installed", e);
        }
    }

    private static void uploadPlugin(URL url, Credentials credentials, File file) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(new URL(url, "pluginManager/uploadPlugin").toExternalForm());
            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("upfile", fileBody);
            post.setEntity(builder.build());
            HttpResponse response = httpClient.execute(post, HttpUtils.buildHttpClientContext(url, credentials));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 302) { // Redirect to updateCenter
                throw new IOException("Could not upload plugin, received " + statusCode + ": " + EntityUtils.toString(response.getEntity()));
            }
        }
    }

    private boolean isFormPathElementPluginInstalled(URL url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(new URL(url, "pluginManager/api/json").toExternalForm());

            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("depth", "1"));
            parameters.add(new BasicNameValuePair("tree", "plugins[shortName,version]"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post, HttpUtils.buildHttpClientContext(url, credentials));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new IOException("Could not check for form-element-path presence, received " + statusCode + ": " + EntityUtils.toString(response.getEntity()));
            }
            HttpEntity resEntity = response.getEntity();
            String responseBody = EntityUtils.toString(resEntity);

            return responseBody.contains("form-element-path");
        }
    }
}
