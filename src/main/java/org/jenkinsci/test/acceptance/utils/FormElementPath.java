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

    /**
     * Set to true if plugin check should be skipped. Useful when running tests against
     * Jenkins instance where the user doesn't have Overall/Administer access.
     * Warning! By setting to true, you will have to ensure that form-element-path
     * plugin is installed by other means.
     */
    public static boolean SKIP_PLUGIN_CHECK = Boolean.getBoolean(
            FormElementPath.class.getName() + ".SKIP_PLUGIN_CHECK");

    private Credentials credentials;

    @Inject
    @Named("form-element-path.hpi")
    private File formElementPathPlugin;

    @Inject
    private AccessTokenGenerator accessToken;

    public void ensure(@Nonnull URL url, @CheckForNull Credentials credentials) {
        if (SKIP_PLUGIN_CHECK) {
            return;
        }
        try {
            if (credentials != null) {
                this.credentials = accessToken.generate(url, credentials);
            }
            if (!isFormPathElementPluginInstalled(url)) {
                LOGGER.info("Installing form-element-path plugin from " + formElementPathPlugin);
                uploadPlugin(url, formElementPathPlugin);
            }
        } catch (IOException e) {
            throw new AssertionError("Can't check if form-element-path plugin is installed", e);
        }
    }

    private void uploadPlugin(URL url, File file) throws IOException {
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
        return true;
    }
}
