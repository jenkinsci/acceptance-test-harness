package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.credentials.*;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;

/**
 * @author Vivek Pandey
 */
//@WithPlugins("credentials@1.5")
@WithPlugins("credentials@2.0.7")
public class CredentialsTest extends AbstractJUnitTest {
    private static final String GLOBAL_SCOPE = "GLOBAL";
    private static final String SYSTEM_SCOPE = "SYSTEM";
    
    private static final String CRED_USER = "user";
    private static final String CRED_PWD = "password";
    
    @Test @WithPlugins("ssh-credentials")
    public void createSshKeys() throws Exception {
        CredentialsPage cp = new CredentialsPage(jenkins, "_");

        cp.open();
        SshPrivateKeyCredential sc = cp.add(SshPrivateKeyCredential.class);
        sc.username.set(CRED_USER);
        sc.selectEnterDirectly().privateKey.set(CRED_PWD);
        sc.description.set("ssh_creds");
        cp.create();

        //now verify
        final ManagedCredentials c = new ManagedCredentials(jenkins);
        String href = c.credentialById("ssh_creds");
        cp.setConfigUrl(href);
        verifyValueForCredential(cp, sc.username, CRED_USER);
        verifyValueForCredential(cp, sc.selectEnterDirectly().privateKey, CRED_PWD);

    }

    @Test
    public void createUserPwd() {
        // Create credential inside the domain
        CredentialsPage cp = new CredentialsPage(jenkins, "_");
        cp.open();
        UserPwdCredential credInDomain = createUserPwdCredential(cp.add(UserPwdCredential.class), CRED_USER, CRED_PWD, "descr", SYSTEM_SCOPE);
        cp.create();
        ManagedCredentials mc = new ManagedCredentials(jenkins, "_");
        verifyValueInDomain("_", mc.checkIfCredentialsExist("descr"), CRED_USER);
    }
    
    @Test
    public void manageSystemScopedCredentialsTest() throws Exception {
        createUpdateDeleteTest(SYSTEM_SCOPE);
    }

    @Test
    public void manageGlobalScopedCredentialsTest() throws Exception {
        createUpdateDeleteTest(GLOBAL_SCOPE);
    }
    
    private void createUpdateDeleteTest(String systemScope) throws Exception {
        final ManagedCredentials c = new ManagedCredentials(jenkins, "_");

        // Create credential
        CredentialsPage cp = new CredentialsPage(jenkins, "_");
        cp.open();
        UserPwdCredential upc = createUserPwdCredential(cp.add(UserPwdCredential.class), CRED_USER, CRED_PWD, systemScope, systemScope);
        cp.create();

        // verify credential was created
        c.open();
        verifyValueInDomain("_", c.checkIfCredentialsExist(systemScope), CRED_USER);
        
        // Update credential
        String href = c.credentialById(systemScope);
        cp.setConfigUrl(href);
        cp.configure();
        String credUserModified = CRED_USER + "-Modified";
        upc.username.set(credUserModified);
        cp.save();
        
        // verify credential was updated
        c.open();
        verifyValueInDomain("_", c.checkIfCredentialsExist(systemScope), credUserModified);
        
        // Remove credential 
        cp.delete();
        
        // verify credential is not present
        verifyCredentialNotPresent("_", upc.username);
    }

    @Test
    public void manageDomainCredentialsTest() throws Exception {
        final ManagedCredentials mc = new ManagedCredentials(jenkins);

        final String domainName = "domain";
        // Create domain
        final DomainPage dp = new DomainPage(jenkins);
        dp.open();
        Domain d = dp.addDomain();
        d.name.set(domainName);
        d.description.set("domain description");
        dp.save();

        verifyValueForElement(mc.checkSystemPage(domainName), domainName);

        // Create credential inside the domain
        CredentialsPage cp = new CredentialsPage(jenkins, domainName);
        cp.open();
        UserPwdCredential credInDomain = createUserPwdCredential(cp.add(UserPwdCredential.class), CRED_USER, CRED_PWD, "descr", SYSTEM_SCOPE);
        cp.create();
        ManagedCredentials listed = new ManagedCredentials(jenkins, domainName);
        verifyValueInDomain(domainName, listed.checkIfCredentialsExist("descr"), CRED_USER);

        // Update credential inside the domain
        String href = listed.credentialById("descr");
        cp.setConfigUrl(href);
        cp.configure();
        String credUserModified = CRED_USER + "-Modified";
        credInDomain.username.set(credUserModified);
        cp.save();
        listed.open();
        verifyValueInDomain(domainName, listed.checkIfCredentialsExist("descr"), credUserModified);

        // Remove credential 
        cp.delete();
        
        // verify credential is not present
        verifyCredentialNotPresent(domainName, credInDomain.username);
    }
    
    @Test
    public void domainScopedAndGlobalDomainCredentialsTest() {
        final String domainName = "domain";
        final String domainCredUser = "domainUser";
        final String globalCredUser = "globalUser";
        
        // Create domain and credential inside the domain
        final DomainPage dp = new DomainPage(jenkins);
        dp.open();
        Domain d = dp.addDomain();
        d.name.set(domainName);
        d.description.set("domain description");
        dp.save();

        CredentialsPage cp = new CredentialsPage(jenkins, domainName);
        cp.open();
        UserPwdCredential credInDomain = createUserPwdCredential(cp.add(UserPwdCredential.class), domainCredUser, CRED_PWD, "descr", SYSTEM_SCOPE);
        cp.create();
        ManagedCredentials listed = new ManagedCredentials(jenkins, domainName);
        verifyValueInDomain(domainName, listed.checkIfCredentialsExist("descr"), domainCredUser);


        // Create global domain credential
        CredentialsPage cp2 = new CredentialsPage(jenkins, "_");
        cp2.open();
        UserPwdCredential globalCred = createUserPwdCredential(cp2.add(UserPwdCredential.class), globalCredUser, CRED_PWD, "descr", SYSTEM_SCOPE);
        cp2.create();
        ManagedCredentials global = new ManagedCredentials(jenkins, "_");

        verifyValueInDomain(domainName, listed.checkIfCredentialsExist("descr"), domainCredUser);
        verifyValueInDomain("_", global.checkIfCredentialsExist("descr"), globalCredUser);
    }

    private void verifyValueForElement(Control element, String expected) {
        jenkins.visit("credentials/store/system/");
        assert(element.exists());
        assertThat(element.resolve().getText(), containsString(expected));
    }
    /**
     * Only used to check the value of credentials.  Values can only be accessed through the CredentialsPage/update.
     * Must set up the configUrl before calling this method.
     * @param element
     * @param expected
     */
    private void verifyValueForCredential(CredentialsPage cp, Control element, String expected) {
        cp.configure();
        assert(element.exists());
        assertThat(element.resolve().getAttribute("value"), containsString(expected));
    }

    private void verifyValueInDomain(String domain, Control element, String expected) {
        jenkins.visit("credentials/store/system/domain/"+domain);
        assert(element.exists());
        assertThat(element.resolve().getText(), containsString(expected));
    }
    
    private void verifyCredentialNotPresent(String domain, Control element) {
        jenkins.visit("credentials/store/system/domain/"+domain);
        assertFalse(element.exists());
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
    private UserPwdCredential createUserPwdCredential(final UserPwdCredential c, String user, String pwd, String descr, String scope) {
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
}
