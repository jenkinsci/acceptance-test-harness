package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.credentials.Domain;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

/**
 * @author Vivek Pandey
 */
@WithPlugins("credentials@1.5")
public class CredentialsTest extends AbstractJUnitTest {
    private final static String GLOBAL_SCOPE = "GLOBAL";
    private final static String SYSTEM_SCOPE = "SYSTEM";
    
    private final static String USERNAME_INPUT = "_.username";
    private static final String PRIVATE_KEY_INPUT = "_.privateKey";
    
    private static final String CRED_ID = "ID";
    private static final String CRED_USER = "user";
    private static final String CRED_PWD = "password";
    
    @Test @WithPlugins("ssh-credentials")
    public void createSshKeys() {
        final ManagedCredentials c = new ManagedCredentials(jenkins);

        c.open();
        final SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
        sc.username.set(CRED_USER);
        sc.selectEnterDirectly().privateKey.set(CRED_PWD);
        sc.setId(CRED_ID);
        c.save();

        //now verify
        verifyInputValue(CRED_ID, USERNAME_INPUT, CRED_USER);
        verifyInputValue(CRED_ID, PRIVATE_KEY_INPUT, CRED_PWD);
    }

    @Test
    public void createUserPwd() {
        final ManagedCredentials c = new ManagedCredentials(jenkins);

        c.open();
        final UserPwdCredential upc = c.add(UserPwdCredential.class);
        upc.username.set(CRED_USER);
        upc.password.set(CRED_PWD);
        upc.setId(CRED_ID);
        c.save();

        //now verify
        jenkins.visit("credentials");
        verifyInputValue(CRED_ID, USERNAME_INPUT, CRED_USER);
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
        UserPwdCredential upc = c.add(UserPwdCredential.class);
        upc.username.set(CRED_USER);
        upc.password.set(CRED_PWD);
        upc.description.set("Credential description");
        upc.scope.select(systemScope);
        upc.setId(CRED_ID);
        c.save();

        // verify credential was created
        verifyInputValue(CRED_ID, USERNAME_INPUT, CRED_USER);
        
        // Update credential
        c.open();
        upc = c.get(UserPwdCredential.class, CRED_ID);
        final String usernameModified = CRED_USER + "-Modified";
        upc.username.set(usernameModified);
        c.save();
        
        // verify credential was updated
        verifyInputValue(CRED_ID, USERNAME_INPUT, usernameModified);
        
        // Remove credential 
        c.open();
        upc = c.get(UserPwdCredential.class, CRED_ID);
        upc.delete.click();
        c.save();
        
        // verify credential is not present
        verifyCredentialNotPresent(CRED_ID);
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
        UserPwdCredential credInDomain = d.addCredential(UserPwdCredential.class);
        credInDomain.username.set(CRED_USER);
        credInDomain.password.set("cPwd");
        credInDomain.description.set("cDesc");
        credInDomain.scope.select(SYSTEM_SCOPE);
        credInDomain.setId(CRED_ID);
        c.save();
        
        verifyInputValueInDomain(domainName, CRED_ID, USERNAME_INPUT, CRED_USER);
        
        // Update credential inside the domain
        c.open();
        credInDomain = c.get(UserPwdCredential.class, domainName, CRED_ID);
        String credUserModified = CRED_USER + "-Modified";
        credInDomain.username.set(credUserModified);
        c.save();
        
        verifyInputValueInDomain(domainName, CRED_ID, USERNAME_INPUT, credUserModified);
        
        // Remove credential 
        c.open();
        credInDomain = c.get(UserPwdCredential.class, domainName, CRED_ID);
        credInDomain.delete.click();
        c.save();
        
        // verify credential is not present
        verifyCredentialNotPresent(domainName, CRED_ID);
    }

    /**
     * Verifies that a given input for a given credential id 
     * is equals to the value expected. If there are two credentials
     * with the same id (that should not happen), the first one is verified.
     * 
     * @param credId
     * @param inputId
     * @param expected
     */
    private void verifyInputValue(String credId, String inputId, String expected) {
        jenkins.visit("credentials");
        // Find credential div
        String actual = findIfNotVisible(by.input(credId))
                        .findElement(by.ancestor("div"))
                        // Find input to verify inside credential div
                        .findElement(by.input(inputId))
                        .getAttribute("value");
        assertThat(actual, equalTo(expected));
    }
    
    /**
     * Verifies that a given credential is not present in the entire credentials page
     * 
     * @param credId
     */
    private void verifyCredentialNotPresent(String credId) {
        assertThat(getElement(by.input(credId)), nullValue());        
    }
    
    /**
     * Verifies that a given input for a given credential id 
     * is equals to the value expected under the scope of a given domain.
     * If there are two credentials with the same id in the same domain,
     * the first one is verified.
     *
     * @param domainName
     * @param credId
     * @param inputId
     * @param expected
     */
    private void verifyInputValueInDomain(String domainName, String credId, String inputId, String expected) {
        jenkins.visit("credentials");
        
        // Find domain div
        String actual = findIfNotVisible(by.input(domainName))
                        .findElement(by.ancestor("div"))
                         // Find credential div inside domain div
                        .findElement(by.input(credId))
                        .findElement(by.ancestor("div"))
                         // Find input with inputId passed
                        .findElement(by.input(inputId))
                        .getAttribute("value");
        
        assertThat(actual, equalTo(expected));
    }
    
    /**
     * Verifies that a given credential is not present under the scope of a given domain.
     * 
     * @param domainName
     * @param credId
     */
    private void verifyCredentialNotPresent(String domainName, String credId) {
       try {
           // Find domain div
            findIfNotVisible(by.input(domainName))
           .findElement(by.ancestor("div"))
            // Find credential div inside domain div
           .findElement(by.input(credId))
           .findElement(by.ancestor("div"))
           // Find input with credential id passed as parameter
           .findElement(by.input(credId));
       } catch (NoSuchElementException e) {
           // If element is not found, verification is correct
           return;
       }
       // If element was found, fail
       fail();
    }
}
