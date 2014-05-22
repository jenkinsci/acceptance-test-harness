package org.jenkinsci.test.acceptance.update_center;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Parses update-center.json.html possibly from a cache and determine plugins to install.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class CachedUpdateCenterMetadataLoader implements Provider<UpdateCenterMetadata>, javax.inject.Provider<UpdateCenterMetadata> {
    UpdateCenterMetadata metadata;

    @Inject(optional=true) @Named("update_center_url_cache")
    File cache = new File(System.getProperty("java.io.tmpdir"), "update-center.json.html");

    @Inject(optional=true) @Named("update_center_url")
    String url = "https://updates.jenkins-ci.org/update-center.json.html";

    @Override
    public UpdateCenterMetadata get() {
        try {
            if (metadata==null) {
                if (!cache.exists() || System.currentTimeMillis()-cache.lastModified() > TimeUnit.DAYS.toMillis(1)) {
                    // load cache
                    HttpClientBuilder builder = HttpClientBuilder.create();
                    if (System.getProperty("http.proxyHost") != null) {
                        builder.setProxy(new HttpHost(
                                System.getProperty("http.proxyHost"),
                                Integer.parseInt(System.getProperty("http.proxyPort")),
                                "http"));
                    }
                    HttpClient client = builder.build();

                    HttpUriRequest request = new HttpGet(url);
                    HttpResponse response = client.execute(request);
                    String data = EntityUtils.toString(response.getEntity());
                    FileUtils.write(cache, data);
                    /*if (System.getProperty("http.proxyHost") != null) {

                    } else {
                        FileUtils.copyURLToFile(new URL(url),cache);
                    }*/
                }
                metadata = UpdateCenterMetadata.parse(cache);
            }
            return metadata;
        } catch (IOException e) {
            throw new AssertionError("Failed to parse update center data of "+url+" at "+cache, e);
        }
    }
}
