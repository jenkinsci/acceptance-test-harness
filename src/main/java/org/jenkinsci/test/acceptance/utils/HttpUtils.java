package org.jenkinsci.test.acceptance.utils;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URL;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;

class HttpUtils {
    private HttpUtils() {}

    public static HttpClientContext buildHttpClientContext(@NonNull URL url, @CheckForNull Credentials credentials) {
        HttpClientContext context = HttpClientContext.create();
        if (credentials != null) {
            HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            AuthCache authCache = new BasicAuthCache();
            authCache.put(targetHost, new BasicScheme());
            // Add AuthCache to the execution context
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            context.setCredentialsProvider(credentialsProvider);
            context.setAuthCache(authCache);
        }
        return context;
    }
}
