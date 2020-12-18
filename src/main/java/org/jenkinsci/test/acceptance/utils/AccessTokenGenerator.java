package org.jenkinsci.test.acceptance.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Takes static credentials and generates access tokens.
 * Handles caching per Jenkins instance and user.
 */
public class AccessTokenGenerator {
    private ObjectMapper om;
    private Map<CacheKey, Credentials> tokenCache = Collections.synchronizedMap(new HashMap<>());

    public AccessTokenGenerator() {
        om = new ObjectMapper();
    }

    public Credentials generate(@Nonnull URL url, @Nonnull Credentials credentials) throws IOException {
        String name = credentials.getUserPrincipal().getName();
        CacheKey key = new CacheKey(url, name);
        if (!tokenCache.containsKey(key)) {
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                Crumb crumb = getCrumb(url, credentials, httpClient);
                ApiTokenResponse apiTokenResponse = generateApiToken(url, credentials, httpClient, crumb);
                tokenCache.put(key, new UsernamePasswordCredentials(name, apiTokenResponse.data.tokenValue));
            }
        }
        return tokenCache.get(key);
    }

    private ApiTokenResponse generateApiToken(@Nonnull URL url, @Nonnull Credentials credentials, CloseableHttpClient httpClient, Crumb crumb) throws IOException {
        HttpPost post = new HttpPost(new URL(url, "me/descriptorByName/jenkins.security.ApiTokenProperty/generateNewToken").toExternalForm());
        post.setHeader(crumb.getCrumbRequestField(), crumb.getCrumb());
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("newTokenName", "ath-" + System.currentTimeMillis()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
        post.setEntity(entity);
        CloseableHttpResponse postResponse = httpClient.execute(post, HttpUtils.buildHttpClientContext(url, credentials));
        return om.readValue(postResponse.getEntity().getContent(), ApiTokenResponse.class);
    }

    private Crumb getCrumb(@Nonnull URL url, @Nonnull Credentials credentials, CloseableHttpClient httpClient) throws IOException {
        HttpGet get = new HttpGet(new URL(url, "crumbIssuer/api/json").toExternalForm());
        CloseableHttpResponse getResponse = httpClient.execute(get, HttpUtils.buildHttpClientContext(url, credentials));
        int statusCode = getResponse.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            return om.readValue(getResponse.getEntity().getContent(), Crumb.class);
        } else {
            throw new IOException("Got status code " + statusCode + " while getting a crumb: " + EntityUtils.toString(getResponse.getEntity()));
        }
    }

    private static class CacheKey {
        private final URL url;
        private final String username;

        public CacheKey(URL url, String username) {
            this.url = url;
            this.username = username;
        }

        public URL getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(url, cacheKey.url) && Objects.equals(username, cacheKey.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, username);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Crumb {
        private String crumb;
        private String crumbRequestField;

        public String getCrumb() {
            return crumb;
        }

        public void setCrumb(String crumb) {
            this.crumb = crumb;
        }

        public String getCrumbRequestField() {
            return crumbRequestField;
        }

        public void setCrumbRequestField(String crumbRequestField) {
            this.crumbRequestField = crumbRequestField;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiTokenResponse {
        private String status;
        private ApiTokenResponse.Data data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public ApiTokenResponse.Data getData() {
            return data;
        }

        public void setData(ApiTokenResponse.Data data) {
            this.data = data;
        }

        private static class Data {
            private String tokenName;
            private String tokenUuid;
            private String tokenValue;

            public String getTokenName() {
                return tokenName;
            }

            public void setTokenName(String tokenName) {
                this.tokenName = tokenName;
            }

            public String getTokenUuid() {
                return tokenUuid;
            }

            public void setTokenUuid(String tokenUuid) {
                this.tokenUuid = tokenUuid;
            }

            public String getTokenValue() {
                return tokenValue;
            }

            public void setTokenValue(String tokenValue) {
                this.tokenValue = tokenValue;
            }
        }
    }
}
