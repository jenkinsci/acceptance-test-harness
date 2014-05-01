package plugins;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;

import org.jclouds.javax.annotation.Nullable;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.LdapContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.po.*;
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
     * "Jenkins is using ldap as security realm" (with user search base %s) (with user search filter %s)
     *
     * @param userSearchBase
     *              optional: user search base (e.g. "ou=People")
     * @param userSearchFilter
     *              optional: user search filter (e.g. "mail={0}")
     */
    private void useLdapAsSecurityRealm(@CheckForNull String userSearchBase, @CheckForNull String userSearchFilter) {
        LdapContainer l = ldap.get();
        LdapDetails ldapDetails = new LdapDetails(l.getHost(), l.getPort(), l.getManagerDn(), l.getManagerPassword(), l.getRootDn());
        if (userSearchBase != null) {
            ldapDetails.setUserSearchBase(userSearchBase);
        }
        if (userSearchFilter != null) {
            ldapDetails.setUserSearchFilter(userSearchFilter);
        }
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        LdapSecurityRealm realm = security.useRealm(LdapSecurityRealm.class);
        realm.configure(ldapDetails);
        security.save();
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
        useLdapAsSecurityRealm(null, null);
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
        useLdapAsSecurityRealm(null, null);
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
        useLdapAsSecurityRealm(null, null);
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
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        LdapSecurityRealm realm = security.useRealm(LdapSecurityRealm.class);
        int freePort = findAvailablePort();
        LdapDetails notRunningLdap = new LdapDetails("localhost", freePort, "cn=admin,dc=jenkins-ci,dc=org", "root", "dc=jenkins-ci,dc=org");
        realm.configure(notRunningLdap);
        security.save();
        // Then
        assertThat(security.open(), hasContent("Unable to connect to localhost:" + freePort));
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        assertThat(jenkins, not(hasLoggedInUser("jenkins")));
    }

    /**
     * Scenario: login with a user which is in organizational unit "People" and user search base is "ou=People"
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm with user search base "ou=People"
     * When I login with user "homer" and password "simpson"
     * Then I will be logged on as user "homer"
     */
    @Test
    public void login_search_base_people_ok() {
        // Given
        useLdapAsSecurityRealm("ou=People", null);
        // When
        Login login = jenkins.login();
        login.doLogin("homer", "cisco");
        // Then
        assertThat(jenkins, hasLoggedInUser("homer"));
    }

    /**
     * Scenario: login with a user which is NOT in organizational unit "People" and user search base is "ou=People"
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm with user search base "ou=People"
     * When I login with user "jenkins" and password "root"
     * Then I will not be logged on as user "jenkins"
     */
    @Test
    public void login_search_base_people_not_found() {
        // Given
        useLdapAsSecurityRealm("ou=People", null);
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, not(hasLoggedInUser("jenkins")));
    }

    /**
     * Scenario: login with email address
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm with user search filter "mail={0}"
     * When I login with email "jenkins@jenkins-ci.org" and password "root"
     * Then I will be logged on as user "jenkins@jenkins-ci.org"
     */
    @Test
    public void login_email_ok() {
        // Given
        useLdapAsSecurityRealm(null, "mail={0}");
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins@jenkins-ci.org", "root");
        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins@jenkins-ci.org"));
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
