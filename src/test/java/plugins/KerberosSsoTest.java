/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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
package plugins;

import com.google.inject.Inject;
import hudson.remoting.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jenkinsci.test.acceptance.FallbackConfig;
import org.jenkinsci.test.acceptance.docker.fixtures.KerberosContainer;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.model.Statement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

/**
 * Run Kerberos SSO tests against the containerized KDC.
 */
@WithPlugins("kerberos-sso")
@Category(DockerTest.class)
@WithDocker
public class KerberosSsoTest extends AbstractJUnitTest {
    private static final String AUTHORIZED = "Username: user; Password: [PROTECTED]; Authenticated: true; Details: null; Granted Authorities: authenticated";

    @Inject
    public DockerContainerHolder<KerberosContainer> kerberos;

    @Inject
    public FailureDiagnostics diag;

    @Inject
    public TestCleaner cleaner;

    @Test
    public void kerberosTicket() throws Exception {
        setupRealmUser();
        KerberosContainer kdc = startKdc();
        configureSso(kdc, false);
        jenkins.logout();

        // Get TGT
        String tokenCache = kdc.getClientTokenCache();

        // Correctly negotiate in browser
        FirefoxDriver negotiatingDriver = getNegotiatingFirefox(kdc, tokenCache);
        negotiatingDriver.get(jenkins.url("/whoAmI").toExternalForm());
        String out = negotiatingDriver.getPageSource();
        assertThat(out, containsString(AUTHORIZED));

        // The global driver is not configured to do so
        jenkins.visit("/whoAmI"); // 401 Unauthorized
        assertThat(driver.getPageSource(), not(containsString(AUTHORIZED)));

        // Non-negotiating request should fail as well
        assertUnauthenticatedRequestIsRejected(getBadassHttpClient());
    }

    private FirefoxDriver getNegotiatingFirefox(KerberosContainer kdc, String tokenCache) {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setAlwaysLoadNoFocusLib(true);
        // Allow auth negotiation for jenkins under test
        profile.setPreference("network.negotiate-auth.trusted-uris", jenkins.url.toExternalForm());
        profile.setPreference("network.negotiate-auth.delegation-uris", jenkins.url.toExternalForm());
        FirefoxBinary binary = new FirefoxBinary();
        // Inject config and TGT
        binary.setEnvironmentProperty("KRB5CCNAME", tokenCache);
        binary.setEnvironmentProperty("KRB5_CONFIG", kdc.getKrb5ConfPath());
        // Turn debug on
        binary.setEnvironmentProperty("KRB5_TRACE", diag.touch("tracelog").getAbsolutePath());
        binary.setEnvironmentProperty("NSPR_LOG_MODULES", "negotiateauth:5");
        binary.setEnvironmentProperty("NSPR_LOG_FILE", diag.touch("firefox.nego.log").getAbsolutePath());

        String display = FallbackConfig.getBrowserDisplay();
        if (display != null) {
            binary.setEnvironmentProperty("DISPLAY", display);
        }
        final FirefoxDriver driver = new FirefoxDriver(binary, profile);
        cleaner.addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    driver.quit();
                } catch (UnreachableBrowserException ex) {
                    System.err.println("Browser died already");
                    ex.printStackTrace();
                }
            }

            @Override public String toString() {
                return "Close Kerberos WebDriver after test";
            }
        });
        return driver;
    }

    @Test
    public void basicAuth() throws Exception {
        setupRealmUser();
        KerberosContainer kdc = startKdc();
        configureSso(kdc, true);

        // I am not able to get the basic auth to work in FF 45.3.0, so using HttpClient instead
        // org.openqa.selenium.UnsupportedCommandException: Unrecognized command: POST /session/466a800f-eaf8-40cf-a9e8-815f5a6e3c32/alert/credentials
        // alert.setCredentials(new UserAndPassword("user", "ATH"));

        CloseableHttpClient httpClient = getBadassHttpClient();

        // No credentials provided
        assertUnauthenticatedRequestIsRejected(httpClient);

        // Correct credentials provided
        HttpGet get = new HttpGet(jenkins.url.toExternalForm() + "/whoAmI");
        get.setHeader("Authorization", "Basic " + Base64.encode("user:ATH".getBytes()));
        CloseableHttpResponse response = httpClient.execute(get);
        String phrase = response.getStatusLine().getReasonPhrase();
        String out = IOUtils.toString(response.getEntity().getContent());
        assertThat(phrase + ": " + out, out, containsString("Full Name"));
        assertThat(phrase + ": " + out, out, containsString("Granted Authorities: authenticated"));
        assertEquals(phrase + ": " + out, "OK", phrase);

        // Incorrect credentials provided
        get = new HttpGet(jenkins.url.toExternalForm() + "/whoAmI");
        get.setHeader("Authorization", "Basic " + Base64.encode("user:WRONG_PASSWD".getBytes()));
        response = httpClient.execute(get);
        assertEquals("Invalid password/token for user: user", response.getStatusLine().getReasonPhrase());
    }

    private void assertUnauthenticatedRequestIsRejected(CloseableHttpClient httpClient) throws IOException {
        HttpGet get = new HttpGet(jenkins.url.toExternalForm());
        CloseableHttpResponse response = httpClient.execute(get);
        assertEquals("Unauthorized", response.getStatusLine().getReasonPhrase());
        assertEquals("Negotiate", response.getHeaders("WWW-Authenticate")[0].getValue());
    }

    /**
     * HTTP client that does not negotiate.
     */
    private CloseableHttpClient getBadassHttpClient() {
        return HttpClientBuilder.create().setDefaultAuthSchemeRegistry(
                RegistryBuilder.<AuthSchemeProvider>create()
                    .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                    .build()
        ).build();
    }

    /**
     * Turn the SSO on in Jenkins.
     *
     * @param allowBasic Allow basic authentication.
     */
    private void configureSso(KerberosContainer kdc, boolean allowBasic) {
        // Turn Jenkins side debugging on
        jenkins.runScript("System.setProperty('sun.security.krb5.debug', 'true'); System.setProperty('sun.security.spnego.debug', 'true'); return 42");

        JenkinsConfig config = jenkins.getConfigPage();
        config.configure();
        KerberosGlobalConfig kgc = new KerberosGlobalConfig(config);
        kgc.enable();
        kgc.krb5Conf(kdc.getKrb5ConfPath());
        kgc.loginConf(kdc.getLoginConfPath());
        kgc.allowLocalhost(false);
        kgc.allowBasic(allowBasic);

        config.save();
    }

    /**
     * Start KDC container populating target dir with generated keytabs and config files.
     */
    private KerberosContainer startKdc() throws IOException {
        KerberosContainer kdc = kerberos.get();
        File target = diag.mkdirs("target"); // Keep the data for diagnostics
        kdc.populateTargetDir(target);
        return kdc;
    }

    /**
     * Create KDC user in backend realm.
     *
     * It is necessary the backend realm recognises all the users kerberos let in. The user is logged in when this method completes.
     */
    private User setupRealmUser() {
        // Populate realm with users
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.configure();
        JenkinsDatabaseSecurityRealm realm = sc.useRealm(JenkinsDatabaseSecurityRealm.class);
        sc.save();
        // It seems the password needs to be the same as in kerberos
        return realm.signup("user", "ATH", "Full Name", "email@mailinator.com");
    }

    private class KerberosGlobalConfig extends PageAreaImpl {
        public KerberosGlobalConfig(JenkinsConfig config) {
            super(config, "/jenkins-model-GlobalPluginConfiguration/plugin/enabled");
        }

        public KerberosGlobalConfig enable() {
            control("").check();
            return this;
        }

        public KerberosGlobalConfig krb5Conf(String krb5) {
            control("krb5Location").set(krb5);
            return this;
        }

        public KerberosGlobalConfig loginConf(String krb5) {
            control("loginLocation").set(krb5);
            return this;
        }

        public KerberosGlobalConfig allowLocalhost(boolean allow) {
            control("allowLocalhost").check(allow);
            return this;
        }

        public KerberosGlobalConfig allowBasic(boolean allow) {
            control("allowBasic").check(allow);
            return this;
        }
    }
}
