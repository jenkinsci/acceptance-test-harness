/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.plugins.credentials;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Client to interact with Credentials REST API.
 */
public class CredentialsRESTClient {

    private static final String BASE_REST_URL = "credentials/store/system";
    private static final String BASE_DOMAIN_URL = BASE_REST_URL + "/domain";

    private static final String BASE_CREDENTIALS_URL = BASE_DOMAIN_URL + "/_/credential/%s/config.xml";
    private static final String CREATE_CREDENTIALS_URL = BASE_DOMAIN_URL + "/_/createCredentials";
    private static final String BASE_DOMAINS_URL = BASE_DOMAIN_URL + "/%s/config.xml";
    private static final String CREATE_DOMAINS_URL = BASE_REST_URL + "/createDomain";

    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE = "application/xml;charset=utf-8";

    private final HttpClient httpClient;
    private final URL jenkinsUrl;


    public CredentialsRESTClient(final URL jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
        this.httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
    }

    public HttpResponse getCredential(final String id) throws IOException {
        final URL getUrl = urlFromJenkinsBase(BASE_CREDENTIALS_URL, id);
        final HttpGet get = new HttpGet(getUrl.toExternalForm());
        return this.httpClient.execute(get);
    }

    public HttpResponse createCredential(final String id, final String user, final String pwd, final String scope) throws IOException {
        final String payload = "<com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>\n"
                + "  <scope>" + scope + "</scope>\n"
                + "  <id>" + id + "</id>\n"
                + "  <username>" + user + "</username>\n"
                + "  <password>" + pwd + "</password>\n"
                + "</com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>";

        final URL postUrl = urlFromJenkinsBase(CREATE_CREDENTIALS_URL);
        final HttpPost post = new HttpPost(postUrl.toExternalForm());
        post.setHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE);
        post.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));

        return this.httpClient.execute(post);
    }

    public HttpResponse updateCredential(final String id, final String user, final String pwd, final String descr, final String scope) throws IOException {
        final String payload = "<com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>\n"
                + "  <scope>" + scope + "</scope>\n"
                + "  <id>" + id + "</id>\n"
                + "  <username>" + user + "</username>\n"
                + "  <password>" + pwd + "</password>\n"
                + "  <description>" + descr + "</description>\n"
                + "</com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>";

        final URL postUrl = urlFromJenkinsBase(BASE_CREDENTIALS_URL, id);
        final HttpPost post = new HttpPost(postUrl.toExternalForm());
        post.setHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE);
        post.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));

        return this.httpClient.execute(post);
    }

    public HttpResponse deleteCredential(final String id) throws IOException {
        final URL deleteUrl = urlFromJenkinsBase(BASE_CREDENTIALS_URL, id);
        final HttpDelete delete = new HttpDelete(deleteUrl.toExternalForm());
        return this.httpClient.execute(delete);
    }

    public HttpResponse getDomain(final String name) throws IOException {
        final URL getUrl = urlFromJenkinsBase(BASE_DOMAINS_URL, name);
        final HttpGet get = new HttpGet(getUrl.toExternalForm());
        return this.httpClient.execute(get);
    }

    public HttpResponse createDomain(final String name) throws IOException {
        final String payload = "<com.cloudbees.plugins.credentials.domains.Domain>\n"
                + "  <name>" + name + "</name>\n"
                + "</com.cloudbees.plugins.credentials.domains.Domain>";

        final URL postUrl = urlFromJenkinsBase(CREATE_DOMAINS_URL);
        final HttpPost post = new HttpPost(postUrl.toExternalForm());
        post.setHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE);
        post.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));

        return this.httpClient.execute(post);
    }

    public HttpResponse updateDomain(final String name, final String descr) throws IOException {
        final String payload = "<com.cloudbees.plugins.credentials.domains.Domain>\n"
                + "  <name>" + name + "</name>\n"
                + "  <description>" + descr + "</description>\n"
                + "</com.cloudbees.plugins.credentials.domains.Domain>";

        final URL postUrl = urlFromJenkinsBase(BASE_DOMAINS_URL, name);
        final HttpPost post = new HttpPost(postUrl.toExternalForm());
        post.setHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE);
        post.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));

        return this.httpClient.execute(post);
    }

    public HttpResponse deleteDomain(final String name) throws IOException {
        final URL deleteUrl = urlFromJenkinsBase(BASE_DOMAINS_URL, name);
        final HttpDelete delete = new HttpDelete(deleteUrl.toExternalForm());
        return this.httpClient.execute(delete);
    }

    private URL urlFromJenkinsBase(final String rel) {
        try {
            return new URL(this.jenkinsUrl, rel);
        }
        catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private URL urlFromJenkinsBase(final String rel, final String... parameters) {
        return this.urlFromJenkinsBase(String.format(rel, parameters));
    }

}
