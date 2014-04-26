package plugins;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.LdapContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Login;
import org.jenkinsci.test.acceptance.po.SecurityConfig;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
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
        sc.configureLdapAndSave(ldapDetails);
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
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins"));
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
        login.doLogin("jenkins", "thisisawrongpassword");
        // Then
        assertThat(jenkins, not(hasLoggedInUser("jenkins")));
    }

    /**
     * Scenario: Login with ldap uid and a not existent user
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm
     * When I login with user "maggie" and password "simpson"
     * Then I will not be logged on as user "maggie"
     */
    @Test
    public void login_no_such_user() {
        // Given
        useLdapAsSecurityRealm();
        // When
        Login login = jenkins.login();
        login.doLogin("maggie", "simpson");
        // Then
        assertThat(jenkins, not(hasLoggedInUser("maggie")));
    }

    /**
     * Scenario: No ldap server running
     * Given docker fixture "ldap" is not running
     * When I configure Jenkins to use a not running ldap host as security realm
     * Then Jenkins will tell me he cannot connect to "ldap"
     * And I will not be able to login with user "jenkins" and password "root"
     */
    @Test
    public void login_no_ldap() {
        // Given
        // don't start docker fixture here
        // When
        SecurityConfig securityConfig = jenkins.configureSecurity();
        int freePort = findAvailablePort();
        LdapDetails notRunningLdap = new LdapDetails("localhost", freePort, "cn=admin,dc=jenkins-ci,dc=org", "root", "dc=jenkins-ci,dc=org");
        securityConfig.configureLdapAndApply(notRunningLdap);
        // Then
        assertThat(securityConfig.open(), hasContent("Unable to connect to localhost:" + freePort));
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        assertThat(jenkins, not(hasLoggedInUser("jenkins")));

    }

    private int findAvailablePort() {
        // use ldap port 389 as fallback (but maybe there is a ldap server running)
        int port = 389;
        try (ServerSocket s = new ServerSocket(0)){
            port = s.getLocalPort();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }
}
