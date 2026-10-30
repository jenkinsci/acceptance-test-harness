package core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.Domain;
import org.jenkinsci.test.acceptance.plugins.credentials.DomainPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * @author Vivek Pandey
 */
@WithPlugins("credentials")
public class CredentialsTest extends AbstractJUnitTest {
    private static final String GLOBAL_SCOPE = "GLOBAL";
    private static final String SYSTEM_SCOPE = "SYSTEM";

    private static final String JENKINS_USER = "juser";

    private static final String CRED_DOMAIN = "mydomain";
    private static final String CRED_USER = "user";
    private static final String CRED_PWD = "password";
    private static final String CRED_DESCR = "My-super-unique-description";

    @Test
    @WithPlugins("ssh-credentials")
    public void createSshKeys() throws Exception {
        CredentialsPage cp = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);

        cp.open();
        SshPrivateKeyCredential sc = cp.add(SshPrivateKeyCredential.class);
        sc.username.set(CRED_USER);
        sc.selectEnterDirectly().privateKey.set(CRED_PWD);
        sc.description.set("ssh_creds");
        cp.create();

        // now verify
        final ManagedCredentials c = new ManagedCredentials(jenkins);
        String href = c.credentialById("ssh_creds");
        cp.setConfigUrl(href);
        verifyValueForCredential(cp, sc.username, CRED_USER);
        verifyValueForCredentialKey(sc, CRED_PWD, false);
    }

    @Test
    public void createUserPwd() {
        this.createUserPwdCredential(ManagedCredentials.DEFAULT_DOMAIN, null, SYSTEM_SCOPE, null, null);
        ManagedCredentials mc = new ManagedCredentials(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        verifyValueInDomain(ManagedCredentials.DEFAULT_DOMAIN, null, mc.checkIfCredentialsExist(CRED_DESCR), CRED_USER);
    }

    @Test
    public void manageSystemScopedCredentialsTest() throws Exception {
        createUpdateDeleteTest(null, ManagedCredentials.DEFAULT_DOMAIN, SYSTEM_SCOPE);
    }

    @Test
    public void manageGlobalScopedDomainCredentialsTest() throws Exception {
        createUpdateDeleteTest(null, CRED_DOMAIN, GLOBAL_SCOPE);
    }

    @Test
    public void manageGlobalScopedCredentialsTest() throws Exception {
        createUpdateDeleteTest(null, ManagedCredentials.DEFAULT_DOMAIN, GLOBAL_SCOPE);
    }

    @Test
    public void manageSystemScopedDomainCredentialsTest() throws Exception {
        createUpdateDeleteTest(null, CRED_DOMAIN, SYSTEM_SCOPE);
    }

    @Test
    public void managePersonalScopedCredentialsTest() throws Exception {
        this.createUserAndLogin();
        this.createUpdateDeleteTest(JENKINS_USER, ManagedCredentials.DEFAULT_DOMAIN, null);
    }

    private void createUpdateDeleteTest(String user, String domainName, String systemScope) throws Exception {
        ManagedCredentials c;

        if (user != null) {
            c = new ManagedCredentials(jenkins, ManagedCredentials.DEFAULT_DOMAIN, JENKINS_USER);
        } else {
            if (ManagedCredentials.DEFAULT_DOMAIN.equals(domainName)) {
                c = new ManagedCredentials(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
            } else {
                c = new ManagedCredentials(jenkins);
                this.createDomain(domainName);

                verifyValueForElement(c.checkSystemPage(domainName), domainName);
                c = new ManagedCredentials(jenkins, domainName);
            }
        }

        // Create credential
        UserPwdCredential upc = this.createUserPwdCredential(domainName, user, systemScope, null, null);
        CredentialsPage cp = new CredentialsPage(jenkins, domainName, user);

        // verify credential was created
        verifyValueInDomain(domainName, user, c.checkIfCredentialsExist(CRED_DESCR), CRED_USER);

        // Update credential
        String href = c.credentialById(CRED_DESCR);
        cp.setConfigUrl(href);
        cp.configure();
        String credUserModified = CRED_USER + "-Modified";
        upc.username.set(credUserModified);
        cp.save();

        // verify credential was updated
        c.open();
        verifyValueInDomain(domainName, user, c.checkIfCredentialsExist(CRED_DESCR), credUserModified);

        // Remove credential
        cp.delete();

        // verify credential is not present
        verifyCredentialNotPresent(domainName, user, CRED_DESCR, c);
    }

    @Test
    public void manageDomainsTest() {
        ManagedCredentials c = new ManagedCredentials(jenkins);

        Domain d = this.createDomain(CRED_DOMAIN);

        verifyValueForElement(c.checkSystemPage(CRED_DOMAIN), CRED_DOMAIN);

        String domainNameModified = CRED_DOMAIN + "-Modified";

        DomainPage dp = new DomainPage(jenkins, CRED_DOMAIN);
        dp.configure();
        d.name.set(domainNameModified);
        dp.save();

        verifyElementNotPresent(c.checkSystemPage(CRED_DOMAIN));
        verifyValueForElement(c.checkSystemPage(domainNameModified), domainNameModified);

        dp = new DomainPage(jenkins, domainNameModified);
        dp.delete();
        verifyElementNotPresent(c.checkSystemPage(domainNameModified));
    }

    @Test
    public void domainScopedAndGlobalDomainCredentialsTest() {
        final String domainCredUser = "domainUser";
        final String globalCredUser = "globalUser";

        this.createDomain(CRED_DOMAIN);
        this.createUserPwdCredential(CRED_DOMAIN, null, SYSTEM_SCOPE, null, domainCredUser);
        this.createUserPwdCredential(ManagedCredentials.DEFAULT_DOMAIN, null, SYSTEM_SCOPE, null, globalCredUser);

        ManagedCredentials listed = new ManagedCredentials(jenkins, CRED_DOMAIN);
        ManagedCredentials global = new ManagedCredentials(jenkins, ManagedCredentials.DEFAULT_DOMAIN);

        verifyValueInDomain(CRED_DOMAIN, null, listed.checkIfCredentialsExist(CRED_DESCR), domainCredUser);
        verifyValueInDomain(
                ManagedCredentials.DEFAULT_DOMAIN, null, global.checkIfCredentialsExist(CRED_DESCR), globalCredUser);
    }

    @Test
    public void credentialsVisibilityTest() {
        this.createUserPwdCredential(ManagedCredentials.DEFAULT_DOMAIN, null, SYSTEM_SCOPE, "credSystem", null);
        this.createUserPwdCredential(ManagedCredentials.DEFAULT_DOMAIN, null, GLOBAL_SCOPE, "credGlobal", null);

        this.createUserAndLogin();
        this.createUserPwdCredential(ManagedCredentials.DEFAULT_DOMAIN, JENKINS_USER, null, "credUser", null);

        jenkins.visit("/user/" + JENKINS_USER + "/credentials");
        ManagedCredentials c = new ManagedCredentials(jenkins);
        assertFalse(c.checkIfCredentialsExist("credSystem").exists());
        assertTrue(c.checkIfCredentialsExist("credGlobal").exists());
        assertTrue(c.checkIfCredentialsExist("credUser").exists());
    }

    private void verifyValueForElement(Control element, String expected) {
        jenkins.visit("credentials/store/system/");
        assert (element.exists());
        assertThat(element.resolve().getText(), containsString(expected));
    }

    private void verifyElementNotPresent(Control element) {
        jenkins.visit("credentials/store/system/");
        assert (!element.exists());
    }

    /**
     * Only used to check the value of credentials.  Values can only be accessed through the CredentialsPage/update.
     * Must set up the configUrl before calling this method.
     * @param element
     * @param expected
     */
    private void verifyValueForCredential(CredentialsPage cp, Control element, String expected) {
        cp.configure();
        assert (element.exists());
        assertThat(element.resolve().getAttribute("value"), containsString(expected));
    }

    private void verifyValueForCredentialKey(
            SshPrivateKeyCredential credential, String expected, boolean isUserScopedCredentials) {
        String id = credential.control(By.name("_.id")).resolve().getAttribute("value");
        String script = String.format(
                "println(com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.class, Jenkins.instance, %s, null).find {it.id == \"%s\" }.privateKey);",
                isUserScopedCredentials ? "hudson.model.User.current().impersonate()" : "null", id);
        assertEquals("Expect private key and real one do not match", expected, jenkins.runScript(script));
    }

    private void verifyValueInDomain(String domain, String user, Control element, String expected) {
        jenkins.visit(this.generateUrlForCredentialsStore(domain, user));
        assert (element.exists());
        assertThat(element.resolve().getText(), containsString(expected));
    }

    private void verifyCredentialNotPresent(String domain, String user, String credDescription, ManagedCredentials mc) {
        jenkins.visit(generateUrlForCredentialsStore(domain, user));

        Control cred = mc.checkIfCredentialsExist(credDescription);
        assertFalse(cred.exists());
    }

    private String generateUrlForCredentialsStore(String domain, String user) {
        if (user == null) {
            return String.format("credentials/store/system/domain/%s", domain);
        } else {
            return String.format("user/%s/credentials/store/user/domain/%s", user, domain);
        }
    }

    private UserPwdCredential createUserPwdCredential(
            String domainName, String user, String scope, String descr, String credUser) {
        String descrToUse = (descr != null) ? descr : CRED_DESCR;
        String credUserToUse = (credUser != null) ? credUser : CRED_USER;

        final CredentialsPage cp;
        if (user != null) {
            cp = new CredentialsPage(jenkins, domainName, user);
        } else {
            cp = new CredentialsPage(jenkins, domainName);
        }

        cp.open();
        UserPwdCredential cred =
                configureUserPwdCredential(cp.add(UserPwdCredential.class), credUserToUse, CRED_PWD, descrToUse, scope);
        cp.create();

        return cred;
    }

    /**
     * Populates a UserPwdCredential with the values passed as parameter
     *
     * @param c The credential
     * @param user The username
     * @param pwd The password
     * @param descr (optional) The description of the credential
     * @param scope (optional) The scope of the credential
     * @return
     */
    private UserPwdCredential configureUserPwdCredential(
            final UserPwdCredential c, String user, String pwd, String descr, String scope) {
        if (descr != null && !descr.isEmpty()) {
            c.description.set(descr);
        }
        if (scope != null && !scope.isEmpty()) {
            c.scope.select(scope);
        }

        c.username.set(user);
        c.password.set(pwd);
        return c;
    }

    private void createUserAndLogin() {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();

        JenkinsDatabaseSecurityRealm realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
        realm.allowUsersToSignUp(true);
        security.save();
        realm.signup(JENKINS_USER);
        jenkins.login().doLogin(JENKINS_USER);
    }

    private Domain createDomain(String name) {
        DomainPage dp = new DomainPage(jenkins);
        dp.open();
        Domain d = dp.addDomain();
        d.name.set(name);
        d.description.set("domain description");
        dp.save();

        return d;
    }
}
