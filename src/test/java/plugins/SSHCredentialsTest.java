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
package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.AbstractCredentialsTest;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.Domain;
import org.jenkinsci.test.acceptance.plugins.credentials.DomainPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Control;
import org.junit.Test;
import org.openqa.selenium.By;

import java.net.MalformedURLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@WithPlugins({"ssh-credentials", "credentials@2.1.5"})
public class SSHCredentialsTest extends AbstractCredentialsTest {

    private static final String CHECK_PERSONAL_CREDENTIAL_STRING = "println(com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.class, Jenkins.instance, hudson.model.User.current().impersonate(), null).find {it.id == \"%s\" }.privateKey);";
    private static final String CHECK_CREDENTIAL_STRING = "println(com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.class, Jenkins.instance, null, null).find {it.id == \"%s\" }.privateKey);";
    @Test
    public void manageSystemScopedSSHCredentialsTest() throws MalformedURLException {
        createUpdateDeleteSSHCredentialsTest(false, SYSTEM_SCOPE);
    }

    @Test
    public void manageGlobalScopedSSHCredentialsTest() throws MalformedURLException {
        createUpdateDeleteSSHCredentialsTest(false, GLOBAL_SCOPE);
    }

    @Test
    public void managePersonalScopedSSHCredentialsTest() throws MalformedURLException {
        createMockUserAndLogin();
        createUpdateDeleteSSHCredentialsTest(true, null);
    }


    private void createUpdateDeleteSSHCredentialsTest(boolean userCredentials, String systemScope) throws MalformedURLException {
        CredentialsPage cp = createCredentialsPage(userCredentials);
        SshPrivateKeyCredential sshc = createCredentials(SshPrivateKeyCredential.class, cp, systemScope);
        prepareForVerify(cp, CRED_DSCR);
        verifyValueForCredential(cp, sshc.username, CRED_USER);
        verifyValueForCredentialKey(sshc, CRED_PWD, userCredentials);

        // Update credential
        cp.configure();
        final String usernameModified = CRED_USER + "-Modified";
        sshc.username.set(usernameModified);
        cp.save();

        // verify credential was updated
        verifyValueForCredential(cp, sshc.username, usernameModified);

        // Remove credential
        cp.open();
        cp.delete();

        // verify credential is not present
        verifyCredentialNotPresent(userCredentials, ManagedCredentials.DEFAULT_DOMAIN, CRED_DSCR);
    }

    @Test
    public void manageDomainSSHCredentialsTest() throws MalformedURLException {
        final String domainName = "domain";
        // Create domain and credential inside the domain
        final DomainPage dp = new DomainPage(jenkins);
        dp.open();
        Domain d = dp.addDomain();
        d.name.set(domainName);
        d.description.set("domain description");
        dp.save();

        CredentialsPage cp = createCredentialsPage(false);
        final SshPrivateKeyCredential credInDomain = (SshPrivateKeyCredential) createCredentials(SshPrivateKeyCredential.class, cp, SYSTEM_SCOPE);

        prepareForVerify(cp, CRED_DSCR);
        verifyValueForCredential(cp, credInDomain.username, CRED_USER);

        // Update credential inside the domain
        cp.configure();
        String credUserModified = CRED_USER + "-Modified";
        credInDomain.username.set(credUserModified);
        cp.save();

        verifyValueForCredential(cp, credInDomain.username, credUserModified);

        // Remove credential
        cp.open();
        cp.delete();

        // verify credential is not present
        verifyCredentialNotPresent(false, domainName, CRED_DSCR);
    }

    private void verifyValueForCredential(CredentialsPage cp, Control element, String expected) throws MalformedURLException {
        cp.configure();
        assert(element.exists());
        assertThat(element.resolve().getAttribute("value"), containsString(expected));
    }
    
    private void verifyCredentialNotPresent(boolean userCredentials, String domain, String credDescription) {
        String url;
        ManagedCredentials mc;
        if (userCredentials) {
            url = String.format("user/%s/credentials/store/user/domain/%s", CRED_USER, domain);
            mc = new ManagedCredentials(jenkins, domain, CRED_USER);
        } else {
            url = String.format("credentials/store/system/domain/%s", domain);
            mc = new ManagedCredentials(jenkins, domain);
        }

        jenkins.visit(url);

        Control cred = mc.checkIfCredentialsExist(credDescription);
        assertFalse(cred.exists());
    }
    
    private void prepareForVerify(CredentialsPage c, String credDscr) throws MalformedURLException {
        c.setConfigUrl(new ManagedCredentials(jenkins).credentialById(credDscr));
    }

    private void verifyValueForCredentialKey(SshPrivateKeyCredential cred, String expected, boolean personalCredential) {
        String id = cred.control(By.name("_.id")).resolve().getAttribute("value");
        String script = String.format(personalCredential ? CHECK_PERSONAL_CREDENTIAL_STRING : CHECK_CREDENTIAL_STRING, id);
        assertEquals("Expected private key and real one do not match", expected, jenkins.runScript(script));
    }
}
