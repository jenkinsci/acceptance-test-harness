package plugins;

import javax.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.LdapContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Login;
import org.jenkinsci.test.acceptance.po.SecurityConfig;
import org.junit.Assert;
import org.junit.Test;

import static org.jenkinsci.test.acceptance.Matchers.*;


/**
 * Feature: Tests for LdapPlugin.
 *
 * @author Michael Prankl
 */
@Native("docker")
public class LdapPluginTest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<LdapContainer> ldap;

    @Inject
    Jenkins jenkins;

    /**
     * "Jenkins is using ldap as security realm"
     */
    private void useLdapAsSecurityRealm() {
        LdapContainer l = ldap.get();
        LdapDetails ldapDetails = new LdapDetails(l.getHost(), l.getPort(), l.getManagerDn(), l.getManagerPassword(), l.getRootDn());
        SecurityConfig sc = jenkins.configureSecurity();
        sc.configureLdap(ldapDetails);
    }

    /**
     * Scenario: Login with ldap uid and password
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm
     * When I login with user "jenkins" and password "root"
     * Then I will be successfully logged on as user "jenkins"
     */
    @Test
    public void login_ok() {
        // Given
        useLdapAsSecurityRealm();
        // When
        Login login = jenkins.login();
        login = login.doLogin("jenkins", "root");
        // Then
        assertTrue(login.isLoginSuccessful());
    }

    /**
     * Scenario: Login with ldap uid and a wrong password
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm
     * When I login with user "jenkins" and password "thisisawrongpassword"
     * Then I will not be logged on as user "jenkins"
     */
    @Test
    public void login_wrong_password() {
        // Given
        useLdapAsSecurityRealm();
        // When
        Login login = jenkins.login();
        login = login.doLogin("jenkins", "thisisawrongpassword");
        // Then
        assertFalse(login.isLoginSuccessful());
    }
}
