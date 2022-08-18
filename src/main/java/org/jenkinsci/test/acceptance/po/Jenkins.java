package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.inject.Injector;
import hudson.util.VersionNumber;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.jenkinsci.test.acceptance.utils.SupportBundleRequest;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Top-level object that acts as an entry point to various systems.
 *
 * This is also the only page object that can be injected since there's always one that points to THE Jenkins instance
 * under test.
 *
 * @author Kohsuke Kawaguchi
 */
public class Jenkins extends Node implements Container {
    private VersionNumber version;
    private JenkinsController controller;

    public final JobsMixIn jobs;
    public final ViewsMixIn views;
    public final SlavesMixIn slaves;

    private Jenkins(Injector injector, URL url) {
        super(injector,url);
        waitForStarted();
        jobs = new JobsMixIn(this);
        views = new ViewsMixIn(this);
        slaves = new SlavesMixIn(this);
    }

    @Override
    public Jenkins getJenkins() {
        return this;
    }

    public Jenkins(Injector injector, JenkinsController controller) {
        this(injector, startAndGetUrl(controller));
        this.controller = controller;
    }

    private static URL startAndGetUrl(JenkinsController controller) {
        try {
            controller.start();
            return controller.getUrl();
        } catch (IOException e) {
            throw new AssertionError("Failed to start JenkinsController",e);
        }
    }

    /**
     * Get the version of Jenkins under test.
     */
    public VersionNumber getVersion() {
        return ObjectUtils.defaultIfNull(version, getVersionNumber());
    }

    private VersionNumber getVersionNumber() {
        String text;
        try {
            URLConnection urlConnection = IOUtil.openConnection(url);
            text = urlConnection.getHeaderField("X-Jenkins");
            if (text == null) {

                String pageText = IOUtils.toString(urlConnection.getInputStream(), StandardCharsets.UTF_8);
                throw new AssertionError(
                        "Application running on " + url + " does not seem to be Jenkins:\n" + pageText
                );
            }
        } catch (IOException ex) {
            throw new AssertionError("Caught an IOException, Jenkins URL was " + url, ex);
        }
        int space = text.indexOf(' ');
        if (space != -1) {
            text = text.substring(0, space);
        }

        return version = new VersionNumber(text);
    }

    /**
     * Wait for Jenkins to become up and running
     */
    public void waitForStarted() {
        waitFor().withTimeout(1, TimeUnit.MINUTES)
                 .ignoring(AssertionError.class)
                 .until(() -> getVersionNumber() != null);
    }

    /**
     * Tells if Jenkins version under test is 1.X
     */
    public boolean isJenkins1X() {
        return getVersion().isOlderThan(new VersionNumber("2.0"));
    }

    /**
     * Access global configuration page.
     */
    public JenkinsConfig getConfigPage() {
        return new JenkinsConfig(this);
    }

    /**
     * Visit login page.
     */
    public Login login(){
        Login login = new Login(this);
        visit(login.url);
        return login;
    }

    /**
     * Visit logout URL.
     */
    public void logout(){
        visit(new Logout(this).url);
    }

    /**
     * Get user currently logged in.
     */
    public User getCurrentUser() {
        return User.getCurrent(this);
    }

    public User getUser(String name) {
        return new User(this, name);
    }

    /**
     * Access the plugin manager page object
     */
    public PluginManager getPluginManager() {
        return new PluginManager(this);
    }

    /**
     * This method always return true.
     * @deprecated Why would you call a method that always returns true?
     */
    @Deprecated
    public boolean canRestart() {
        return true;
    }

    public void restart() {
        try {
            controller.restart();
            waitForLoad(JenkinsController.STARTUP_TIMEOUT);
        } catch (IOException e) {
            throw new IllegalStateException("Could not restart Jenkins", e);
        }
    }

    public void waitForLoad(int seconds){
        List<Class<? extends Throwable>> ignoring = new ArrayList<Class<? extends Throwable>>();
        ignoring.add(AssertionError.class);
        ignoring.add(NoSuchElementException.class);
        ignoring.add(WebDriverException.class);
        //Ignore WebDriverException during restart.
        // Poll until we have the real page
        waitFor(driver).withTimeout(seconds, TimeUnit.SECONDS)
                .ignoreAll(ignoring)
                .until((Function<WebDriver, Boolean>) driver -> {
                    visit(driver.getCurrentUrl()); // the page sometimes does not reload (fast enough)
                    getJson("tree=nodeName"); // HudsonIsRestarting will serve a 503 to the index page, and will refuse api/json
                    return true;
                })
        ;
    }

    public JenkinsLogger getLogger(String name) {
        return new JenkinsLogger(this,name);
    }

    public JenkinsLogger createLogger(String name, Map<String,Level> levels) {
        return JenkinsLogger.create(this,name,levels);
    }

    public Plugin getPlugin(String name) {
        return new Plugin(getPluginManager(), name);
    }

    public <T extends PageObject> T getPluginPage(Class<T> type) {
        String urlChunk = type.getAnnotation(PluginPageObject.class).value();

        return newInstance(type, injector, url("plugin/%s/", urlChunk));
    }

    @Override
    public String getName() {
        return "(master)";
    }

    @Override
    public JobsMixIn getJobs() {
        return jobs;
    }

    @Override
    public ViewsMixIn getViews() {
        return views;
    }

    public void generateSupportBundle(SupportBundleRequest supportBundleRequest) {
        URL url = url("support/generateAllBundles");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(new URIBuilder()
                    .setScheme(url.getProtocol())
                    .setHost(url.getHost())
                    .setPort(url.getPort())
                    .setPath(url.getPath())
                    .setParameter("json", supportBundleRequest.getJsonParameter())
                    .build());
            Crumb crumb = getCrumb(client);
            if (crumb != null) {
                httpPost.setHeader(crumb.getCrumbRequestField(), crumb.getCrumb());
            }
            try (CloseableHttpResponse response = client.execute(httpPost, buildHttpClientContext())) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try (FileOutputStream os = new FileOutputStream(supportBundleRequest.getOutputFile())) {
                            entity.writeTo(os);
                        }
                    }
                } else {
                    throw new IOException("Got status code " + statusCode + " while getting support bundle for " + url);
                }
            }
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    private Crumb getCrumb(CloseableHttpClient httpClient) throws IOException {
        HttpGet get = new HttpGet(url("crumbIssuer/api/json").toExternalForm());
        CloseableHttpResponse getResponse = httpClient.execute(get, buildHttpClientContext());
        int statusCode = getResponse.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            return new ObjectMapper().readValue(getResponse.getEntity().getContent(), Crumb.class);
        } else if (statusCode == 404) {
            // Crumb issuer is disabled
            return null;
        } else {
            throw new IOException("Got status code " + statusCode + " while getting a crumb: " + EntityUtils.toString(getResponse.getEntity()));
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

    private HttpContext buildHttpClientContext() {
        HttpClientContext context = HttpClientContext.create();
        BasicCookieStore cookieStore = new BasicCookieStore();
        for (org.openqa.selenium.Cookie cookie : driver.manage().getCookies()) {
            BasicClientCookie c = new BasicClientCookie(cookie.getName(), cookie.getValue());
            c.setPath(cookie.getPath());
            c.setExpiryDate(cookie.getExpiry());
            c.setDomain(cookie.getDomain());
            cookieStore.addCookie(c);
        }
        context.setCookieStore(cookieStore);
        return context;
    }
}
