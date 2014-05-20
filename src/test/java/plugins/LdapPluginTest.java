package plugins;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.LdapContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.po.*;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;

import static org.hamcrest.CoreMatchers.not;
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
    private void useLdapAsSecurityRealm(LdapDetails ldapDetails) {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        LdapSecurityRealm realm = security.useRealm(LdapSecurityRealm.class);
        realm.configure(ldapDetails);
        security.save();
    }

    /**
     * Creates default ldap connection details from a running docker LdapContainer.
     *
     * @param ldapContainer a docker LdapContainer
     * @return default ldap connection details
     */
    private LdapDetails createDefaults(LdapContainer ldapContainer) {
        LdapDetails details = new LdapDetails(ldapContainer.getHost(), ldapContainer.getPort(), ldapContainer.getManagerDn(), ldapContainer.getManagerPassword(), ldapContainer.getRootDn());
        return details;
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
        useLdapAsSecurityRealm(createDefaults(ldap.get()));
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
        useLdapAsSecurityRealm(createDefaults(ldap.get()));
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
        useLdapAsSecurityRealm(createDefaults(ldap.get()));
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
        useLdapAsSecurityRealm(createDefaults(ldap.get()).userSearchBase("ou=People"));
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
        useLdapAsSecurityRealm(createDefaults(ldap.get()).userSearchBase("ou=People"));
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
        useLdapAsSecurityRealm(createDefaults(ldap.get()).userSearchFilter("mail={0}"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins@jenkins-ci.org", "root");
        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins@jenkins-ci.org"));
    }

    /**
     * Scenario: fallback to alternate server
     * Given I have a docker fixture "ldap"
     * And Jenkins is using a not running ldap server as primary and "ldap" as fallback security realm
     * When I login with "jenkins" and password "root"
     * Then I will be successfully logged in as user "jenkins"
     */
    @Test
    public void login_use_fallback_server() {
        // Given
        LdapContainer ldapContainer = ldap.get();
        GlobalSecurityConfig securityConfig = new GlobalSecurityConfig(jenkins);
        securityConfig.configure();
        LdapSecurityRealm realm = securityConfig.useRealm(LdapSecurityRealm.class);
        int freePort = this.findAvailablePort();
        LdapDetails ldapDetails = new LdapDetails("", 0, ldapContainer.getManagerDn(), ldapContainer.getManagerPassword(), ldapContainer.getRootDn());
        // Fallback-Config: primary server is not running, alternative server is running docker fixture
        ldapDetails.setHostWithPort("localhost:" + freePort + " localhost:" + ldapContainer.getPort());
        realm.configure(ldapDetails);
        securityConfig.save();

        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");

        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins"));

    }

    /**
     * Scenario: resolve email address
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm
     * When I login with user "jenkins" and password "root"
     * Then I will be logged on as user "jenkins"
     * And the resolved mail address is "jenkins@jenkins-ci.org"
     */
    @Test
    public void resolve_email() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins"));
        User u = new User(jenkins, "jenkins");
        assertThat(u, mailAddressIs("jenkins@jenkins-ci.org"));
    }

    /**
     * Scenario: resolve group memberships of user with default configuration
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm
     * When I login with user "jenkins" and password "root"
     * Then "jenkins" will be member of following groups: "ldap1", "ldap2"
     */
    @Test
    public void resolve_group_memberships_with_defaults() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, isMemberOf("ldap1"));
        assertThat(userJenkins, isMemberOf("ldap2"));
    }

    /**
     * Scenario: resolve group memberships of user with default configuration
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm
     * When I login with user "homer" and password "cisco"
     * Then "homer" will be member of group "ldap2"
     * And "homer" will not be member of group "ldap1"
     */
    @Test
    public void resolve_group_memberships_with_defaults_negative() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()));
        // When
        Login login = jenkins.login();
        login.doLogin("homer", "cisco");
        User homer = new User(jenkins, "homer");
        // Then
        assertThat(homer, isMemberOf("ldap2"));
        assertThat(homer, not(isMemberOf("ldap1")));
    }

    /**
     * Scenario: using custom group search base "ou=Applications" (contains no groups)
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm with group search base "ou=Applications"
     * When I login with user "jenkins" and password "root"
     * Then "jenkins" will not be member of groups "ldap1" and "ldap2"
     */
    @Test
    public void custom_group_search_base() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).groupSearchBase("ou=Applications"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, not(isMemberOf("ldap1")));
        assertThat(userJenkins, not(isMemberOf("ldap2")));
    }

    /**
     * Scenario: resolve display name of ldap user with default display name attribute
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm
     * When I login with user "jenkins" and password "root"
     * Then the display name of "jenkins" will be "Jenkins displayname"
     * <p/>
     * working since ldap plugin version: 1.8
     */
    @Test
    @Bug("JENKINS-18355")
    @WithPlugins("ldap")
    public void resolve_display_name_with_defaults() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, fullNameIs("Jenkins displayname"));
    }

    /**
     * Scenario: using custom display name attribute (cn instead of display name)
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm and display name attribute is "cn"
     * When I login with user "jenkins" and password "root"
     * Then the display name of "jenkins" will be "Jenkins the Butler"
     * <p/>
     * working since ldap plugin version: 1.8
     */
    @Test
    @Bug("JENKINS-18355")
    @WithPlugins("ldap")
    public void custom_display_name() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).displayNameAttributeName("cn"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, fullNameIs("Jenkins the Butler"));
    }

    /**
     * Scenario: using custom group membership filter which leads to no user belongs to a group
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm with group membership filter "(member={0})"
     * When I login with user "jenkins" and password "root"
     * Then "jenkins" will not be member of groups "ldap1" and "ldap2"
     */
    @Test
    @WithPlugins("ldap")
    public void custom_group_membership_filter() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).groupMembershipFilter("(member={0})"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, not(isMemberOf("ldap1")));
        assertThat(userJenkins, not(isMemberOf("ldap2")));
    }

    /**
     * Scenario: use a custom mail filter (gn instead of mail)
     * Given I have a docker fixture "ldap"
     * And Jenkins is using ldap as security realm and mail address attribute is "dn"
     * When I login with user "jenkins" and password "root"
     * Then the mail address of "jenkins" will be "givenname@mailaddress.com"
     * <p/>
     * since ldap plugin version 1.8
     */
    @Test
    @WithPlugins("ldap")
    public void custom_mail_filter() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).mailAdressAttributeName("gn"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, mailAddressIs("givenname@mailaddress.com"));
    }

    private int findAvailablePort() {
        // use ldap port 389 as fallback (but maybe there is a ldap server running)
        int port = 389;
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }
}
