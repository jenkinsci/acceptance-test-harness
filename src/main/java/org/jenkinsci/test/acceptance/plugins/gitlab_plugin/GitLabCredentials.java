package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import org.jenkinsci.test.acceptance.plugins.credentials.AbstractCredentialsTest;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.StringCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.By;

import java.net.MalformedURLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

public class GitLabCredentials extends AbstractCredentialsTest {

    private static final String CHECK_PERSONAL_CREDENTIAL_STRING = "println(com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.class, Jenkins.instance, hudson.model.User.current().impersonate(), null).find {it.id == \"%s\" }.privateKey);";
    private static final String CHECK_CREDENTIAL_STRING = "println(com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.class, Jenkins.instance, null, null).find {it.id == \"%s\" }.privateKey);";

    public GitLabCredentials(Jenkins jenkins) {
        this.jenkins = jenkins;
    }
    public void createCredentials(String token, String tokenType)  {

        CredentialsPage mc = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        mc.open();


        mc.addButton.click();

       // find(by.option("GitLab API token")).click();
//        find(by.path("/credentials/apiToken")).sendKeys(token);
//        find(by.path("/credentials/id")).sendKeys("GitLab Personal Access Token");

        mc.create();
    }

    private void verifyValueForCredential(CredentialsPage cp, Control element, String expected) throws MalformedURLException {
        cp.configure();
        assert(element.exists());
        assertThat(element.resolve().getAttribute("value"), containsString(expected));
    }

    private void verifyValueForCredentialKey(SshPrivateKeyCredential cred, String expected, boolean personalCredential) {
        String id = cred.control(By.name("_.id")).resolve().getAttribute("value");
        String script = String.format(personalCredential ? CHECK_PERSONAL_CREDENTIAL_STRING : CHECK_CREDENTIAL_STRING, id);
        assertEquals("Expected private key and real one do not match", expected, jenkins.runScript(script));
    }
}
