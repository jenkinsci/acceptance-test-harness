package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.jenkinsci.test.acceptance.Matchers.*;

import jakarta.inject.Inject;
import java.io.IOException;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.LdapContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapEnvironmentVariable;
import org.jenkinsci.test.acceptance.plugins.ldap.SearchForGroupsLdapGroupMembershipStrategy;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.LdapSecurityRealm;
import org.jenkinsci.test.acceptance.po.Login;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;


/**
 * This test suite always runs against the latest ldap plugin version.
 *
 * @author Michael Prankl
 */
@Category(DockerTest.class)
@WithDocker
@WithPlugins("ldap@1.10")
public class LdapPluginTest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<LdapContainer> ldap;

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
        return ldapContainer.createDefault();
    }
    
    /**
     * Creates default ldap connection details without manager credentials from a running docker LdapContainer.
     * 
     * @param ldapContainer
     * @return default ldap connection details without the manager credentials
     */
    private LdapDetails createDefaultsWithoutManagerCred(LdapContainer ldapContainer) {
        String host = ldapContainer.getHost();
        if (LdapContainer.ipv6Enabled()) {
            host = String.format("[%s]", ldapContainer.getHost());
        }
        return new LdapDetails(host, ldapContainer.getPort(), "", "", ldapContainer.getRootDn());
    }

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

    @Test
    public void login_ok_anonymous_binding() {
        // Given
        useLdapAsSecurityRealm(createDefaultsWithoutManagerCred(ldap.get()));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins"));
    }

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

    @Test
    public void login_no_ldap() throws InterruptedException {
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
        waitFor(security.open(), hasContent("Unable to connect to localhost:" + freePort), 5);
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        assertThat(jenkins, not(hasLoggedInUser("jenkins")));
    }

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
    
    @Test
    public void invalid_user_search_filter() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).userSearchFilter("invalid={0}"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins@jenkins-ci.org", "root");
        // Then
        assertThat(jenkins, not(hasLoggedInUser("jenkins@jenkins-ci.org")));
        // When
        login = jenkins.login();
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, not(hasLoggedInUser("jenkins")));
    }

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
        ldapDetails.setHostWithPort("localhost:" + freePort + ' ' + ldapContainer.getHost() + ':' + ldapContainer.getPort());
        realm.configure(ldapDetails);
        securityConfig.save();

        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");

        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins"));

    }

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

    @Test
    public void do_not_resolve_email() {
        // Given
        LdapDetails details = createDefaults(ldap.get());
        details.setDisableLdapEmailResolver(true);
        useLdapAsSecurityRealm(details);
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins"));
        User u = new User(jenkins, "jenkins");
        assertThat(u.mail(), nullValue());
    }
    
    @Test
    public void enable_cache() throws IOException {
        // Given
        LdapDetails details = createDefaults(ldap.get());
        details.setEnableCache(true);
        useLdapAsSecurityRealm(details);
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, hasLoggedInUser("jenkins"));
    }

    @Test
    public void use_environment_varibales() {
        // Given
        LdapDetails details = createDefaultsWithoutManagerCred(ldap.get());
        details.addEnvironmentVariable(new LdapEnvironmentVariable("java.naming.security.protocol", "ssl"));
        useLdapAsSecurityRealm(details);
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        // Then
        assertThat(jenkins, not(hasLoggedInUser("jenkins")));
    }
   
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

    @Test
    @Issue("JENKINS-18355")
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

    @Test
    @Issue("JENKINS-18355")
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

    @Test
    public void custom_invalid_group_membership_filter() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).groupMembershipStrategy(SearchForGroupsLdapGroupMembershipStrategy.class).groupMembershipStrategyParam("(member={0})"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, not(isMemberOf("ldap1")));
        assertThat(userJenkins, not(isMemberOf("ldap2")));
    }
    
    @Test
    public void custom_valid_group_membership_filter() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).groupMembershipStrategy(SearchForGroupsLdapGroupMembershipStrategy.class).groupMembershipStrategyParam("memberUid={1}"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, isMemberOf("ldap1"));
        assertThat(userJenkins, isMemberOf("ldap2"));
    }
    
    @Test
    public void custom_mail_filter() {
        // Given
        useLdapAsSecurityRealm(createDefaults(ldap.get()).mailAdressAttributeName("givenName"));
        // When
        Login login = jenkins.login();
        login.doLogin("jenkins", "root");
        User userJenkins = new User(jenkins, "jenkins");
        // Then
        assertThat(userJenkins, mailAddressIs("givenname@mailaddress.com"));
    }
}
