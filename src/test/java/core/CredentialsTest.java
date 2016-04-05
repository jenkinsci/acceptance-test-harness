package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.credentials.Domain;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;

/**
 * @author Vivek Pandey
 */
@WithPlugins("credentials@1.5")
public class CredentialsTest extends AbstractJUnitTest {
    private static final String GLOBAL_SCOPE = "GLOBAL";
    private static final String SYSTEM_SCOPE = "SYSTEM";
    
    private static final String CRED_USER = "user";
    private static final String CRED_PWD = "password";
    
    @Test @WithPlugins("ssh-credentials")
    public void createSshKeys() {
        final ManagedCredentials c = new ManagedCredentials(jenkins);

        c.open();
        final SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
        sc.username.set(CRED_USER);
        sc.selectEnterDirectly().privateKey.set(CRED_PWD);
        c.save();

        //now verify
        verifyValueForElement(sc.username, CRED_USER);
        verifyValueForElement(sc.selectEnterDirectly().privateKey, CRED_PWD);
    }

    @Test
    public void createUserPwd() {
        final ManagedCredentials c = new ManagedCredentials(jenkins);

        c.open();
        final UserPwdCredential upc = createUserPwdCredential(c.add(UserPwdCredential.class), CRED_USER, CRED_PWD, null, null);
        c.save();

        //now verify
        jenkins.visit("credentials");
        verifyValueForElement(upc.username, CRED_USER);
    }
    
    @Test
    public void manageSystemScopedCredentialsTest() {
        createUpdateDeleteTest(SYSTEM_SCOPE);
    }

    @Test
    public void manageGlobalScopedCredentialsTest() {
        createUpdateDeleteTest(GLOBAL_SCOPE);
    }
    
    private void createUpdateDeleteTest(String systemScope) {
        final ManagedCredentials c = new ManagedCredentials(jenkins);

        // Create credential
        c.open();
        final UserPwdCredential upc = createUserPwdCredential(c.add(UserPwdCredential.class), CRED_USER, CRED_PWD, "Descr", systemScope);
        c.save();

        // verify credential was created
        verifyValueForElement(upc.username, CRED_USER);
        
        // Update credential
        c.open();
        final String usernameModified = CRED_USER + "-Modified";
        upc.username.set(usernameModified);
        c.save();
        
        // verify credential was updated
        verifyValueForElement(upc.username, usernameModified);
        
        // Remove credential 
        c.open();
        upc.delete();
        c.save();
        
        // verify credential is not present
        verifyElementNotPresent(upc.username);
    }

    @Test
    public void manageDomainCredentialsTest() {
        final String domainName = "domain";
        // Create domain and credential inside the domain
        final ManagedCredentials c = new ManagedCredentials(jenkins);
        c.open();
        Domain d = c.addDomain();
        d.name.set(domainName);
        d.description.set("domain description");
        final UserPwdCredential credInDomain = createUserPwdCredential(d.addCredential(UserPwdCredential.class), CRED_USER, CRED_PWD, "descr", SYSTEM_SCOPE);
        c.save();
        
        verifyValueForElement(credInDomain.username, CRED_USER);
        
        // Update credential inside the domain
        c.open();
        String credUserModified = CRED_USER + "-Modified";
        credInDomain.username.set(credUserModified);
        c.save();
        
        verifyValueForElement(credInDomain.username, credUserModified);
        
        // Remove credential 
        c.open();
        credInDomain.delete();
        c.save();
        
        // verify credential is not present
        verifyElementNotPresent(credInDomain.username);
    }
    
    @Test
    public void domainScopedAndGlobalDomainCredentialsTest() {
        final String domainName = "domain";
        final String domainCredUser = "domainUser";
        final String globalCredUser = "globalUser";
        
        // Create domain and credential inside the domain
        final ManagedCredentials c = new ManagedCredentials(jenkins);
        c.open();
        Domain d = c.addDomain();
        d.name.set(domainName);
        d.description.set("domain description");
        final UserPwdCredential credInDomain = createUserPwdCredential(d.addCredential(UserPwdCredential.class), domainCredUser, CRED_PWD, "descr", SYSTEM_SCOPE);
        c.save();
        
        // Create global domain credential
        c.open();
        final UserPwdCredential globalCred = createUserPwdCredential(c.add(UserPwdCredential.class), globalCredUser, CRED_PWD, "descr", SYSTEM_SCOPE);
        c.save();
        
        jenkins.visit("credentials");
        verifyValueForElement(credInDomain.username, domainCredUser);
        verifyValueForElement(globalCred.username, globalCredUser);
    }

    
    private void verifyValueForElement(Control element, String expected) {
        jenkins.visit("credentials");
        assertThat(element.resolve().getAttribute("value"), equalTo(expected));
    }
    
    private void verifyElementNotPresent(Control element) {
        jenkins.visit("credentials");
        assertFalse(element.exists());
    }

    /**
     * Populates a UserPwdCredential with the values passed as parameter
     * 
     * @param c The credential
     * @param user The username
     * @param pwd The password
     * @param descr (optional) The description of the credential
     * @param systemScope (optional) The scope of the credential
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
