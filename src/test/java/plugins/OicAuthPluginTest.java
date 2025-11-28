package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.LoggedInAuthorizationStrategy;
import org.jenkinsci.test.acceptance.po.OicAuthConfigurationMode;
import org.jenkinsci.test.acceptance.po.OicAuthSecurityRealm;
import org.jenkinsci.test.acceptance.po.WhoAmI;
import org.jenkinsci.test.acceptance.utils.keycloack.KeycloakUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@WithPlugins("oic-auth")
public class OicAuthPluginTest extends AbstractJUnitTest {

    private static final String REALM = "test-realm";
    private static final String CLIENT = "jenkins";

    private static final String KEYCLOAK_IMAGE =
            "keycloak/keycloak:26.4.6@sha256:d0d4037f17521a7f06137afd5a0eecb1f977f4ade773ae7755f1ee82cad8a576";

    @Rule
    public KeycloakContainer keycloak = new KeycloakContainer(KEYCLOAK_IMAGE);

    @Inject
    public KeycloakUtils keycloakUtils;

    private String userBobKeycloakId;
    private String userJohnKeycloakId;

    @Before
    public void setUpKeycloak() throws Exception {
        configureOIDCProvider();
        configureRealm();
    }

    private void configureOIDCProvider() throws Exception {
        try (Keycloak keycloakAdmin = keycloak.getKeycloakAdminClient()) {
            // Create Realm
            RealmRepresentation testRealm = new RealmRepresentation();
            testRealm.setRealm(REALM);
            testRealm.setId(REALM);
            testRealm.setDisplayName(REALM);
            testRealm.setEnabled(true);

            keycloakAdmin.realms().create(testRealm);
            RoleRepresentation jenkinsRead = new RoleRepresentation();
            jenkinsRead.setName("jenkinsRead");
            keycloakAdmin.realm(REALM).roles().create(jenkinsRead);
            RoleRepresentation jenkinsAdmin = new RoleRepresentation();
            jenkinsAdmin.setName("jenkinsAdmin");
            keycloakAdmin.realm(REALM).roles().create(jenkinsAdmin);

            // Add groups and subgroups
            GroupRepresentation employees = new GroupRepresentation();
            employees.setName("employees");

            final RealmResource theRealm = keycloakAdmin.realm(REALM);
            theRealm.groups().add(employees);

            String groupId = theRealm.groups().groups().get(0).getId();

            GroupRepresentation devs = new GroupRepresentation();
            devs.setName("devs");
            GroupResource group = theRealm.groups().group(groupId);
            group.subGroup(devs);

            GroupRepresentation sales = new GroupRepresentation();
            sales.setName("sales");
            group = theRealm.groups().group(groupId);
            group.subGroup(sales);

            List<GroupRepresentation> subGroups =
                    theRealm.groups().group(groupId).getSubGroups(0, 2, true);
            String devsId = subGroups.stream()
                    .filter(g -> g.getName().equals("devs"))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Something went wrong initialization keycloak"))
                    .getId();
            String salesId = subGroups.stream()
                    .filter(g -> g.getName().equals("sales"))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Something went wrong initialization keycloak"))
                    .getId();
            theRealm.groups()
                    .group(devsId)
                    .roles()
                    .realmLevel()
                    .add(List.of(theRealm.roles().get("jenkinsAdmin").toRepresentation()));
            theRealm.groups()
                    .group(salesId)
                    .roles()
                    .realmLevel()
                    .add(List.of(theRealm.roles().get("jenkinsRead").toRepresentation()));

            // Users
            UserRepresentation bob = new UserRepresentation();
            bob.setEmail("bob@jenkins-ath.test");
            bob.setUsername("bob");
            bob.setFirstName("Bob");
            bob.setLastName("Smith");
            CredentialRepresentation credentials = new CredentialRepresentation();
            credentials.setValue("bob");
            credentials.setTemporary(false);
            credentials.setType(CredentialRepresentation.PASSWORD);
            bob.setCredentials(List.of(credentials));
            bob.setGroups(Arrays.asList("/employees", "/employees/devs"));
            bob.setEmailVerified(true);
            bob.setEnabled(true);
            theRealm.users().create(bob);

            UserRepresentation john = new UserRepresentation();
            john.setEmail("john@jenkins-ath.test");
            john.setUsername("john");
            john.setFirstName("John");
            john.setLastName("Smith");
            credentials = new CredentialRepresentation();
            credentials.setValue("john");
            credentials.setTemporary(false);
            credentials.setType(CredentialRepresentation.PASSWORD);
            john.setCredentials(List.of(credentials));
            john.setGroups(Arrays.asList("/employees", "/employees/sales"));
            john.setEmailVerified(true);
            john.setEnabled(true);
            theRealm.users().create(john);

            // Client
            ClientRepresentation jenkinsClient = new ClientRepresentation();
            jenkinsClient.setClientId(CLIENT);
            jenkinsClient.setProtocol("openid-connect");
            jenkinsClient.setSecret(CLIENT);
            jenkinsClient.setServiceAccountsEnabled(true);
            final String jenkinsUrl = jenkins.url.toString();
            jenkinsClient.setRootUrl(jenkinsUrl);
            jenkinsClient.setRedirectUris(List.of(String.format("%ssecurityRealm/finishLogin", jenkinsUrl)));
            jenkinsClient.setWebOrigins(List.of(jenkinsUrl));
            jenkinsClient.setAttributes(Map.of("post.logout.redirect.uris", String.format("%sOicLogout", jenkinsUrl)));
            theRealm.clients().create(jenkinsClient);

            // Assert that the realm is properly created
            assertThat("group is created", theRealm.groups().groups().get(0).getName(), is("employees"));
            GroupResource g = theRealm.groups().group(groupId);
            assertThat(
                    "subgroups are created",
                    g.getSubGroups(0, 2, true).stream()
                            .map(GroupRepresentation::getName)
                            .collect(Collectors.toList()),
                    containsInAnyOrder("devs", "sales"));
            assertThat(
                    "users are created",
                    theRealm.users().list().stream()
                            .map(UserRepresentation::getUsername)
                            .collect(Collectors.toList()),
                    containsInAnyOrder("bob", "john"));
            userBobKeycloakId =
                    theRealm.users().searchByUsername("bob", true).get(0).getId();
            assertThat(
                    "User bob with the correct groups",
                    theRealm.users().get(userBobKeycloakId).groups().stream()
                            .map(GroupRepresentation::getPath)
                            .collect(Collectors.toList()),
                    containsInAnyOrder("/employees", "/employees/devs"));
            userJohnKeycloakId =
                    theRealm.users().searchByUsername("john", true).get(0).getId();
            assertThat(
                    "User john with the correct groups",
                    theRealm.users().get(userJohnKeycloakId).groups().stream()
                            .map(GroupRepresentation::getPath)
                            .collect(Collectors.toList()),
                    containsInAnyOrder("/employees", "/employees/sales"));
            assertThat(
                    "client is created",
                    theRealm.clients().findByClientId(CLIENT).get(0).getProtocol(),
                    is("openid-connect"));
        }
    }

    private void configureRealm() {
        final String keycloakUrl = keycloak.getAuthServerUrl();
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        OicAuthSecurityRealm securityRealm = sc.useRealm(OicAuthSecurityRealm.class);
        securityRealm.configureClient(CLIENT, CLIENT);
        // Configuration mode
        OicAuthConfigurationMode.WellKnownEndpoint configurationMode =
                securityRealm.useConfigurationMode(OicAuthConfigurationMode.WellKnownEndpoint.class);
        configurationMode.wellKnownEndpoint.set(
                String.format("%s/realms/%s/.well-known/openid-configuration", keycloakUrl, REALM));
        securityRealm.setLogoutFromOpenidProvider(true);
        securityRealm.setPostLogoutUrl(jenkins.url("OicLogout").toExternalForm());
        securityRealm.setUserFields(null, null, null, "groups");
        sc.useAuthorizationStrategy(LoggedInAuthorizationStrategy.class);
        sc.save();
    }

    /**
     * Test the login and logout in Jenkins and how it is propagated to the OIDC Provider
     * - A login in Jenkins will redirect to the provider login page, so the user will be logged in as well in keycloak
     * - A logout in Jenkins will apply only to Jenkins or will be propagated to the provider (keycloak here) depending
     *   on the value of the property Lout from openid provider in the Security Realm configuration. For this test,
     *   it'll be propagated
     */
    @Test
    public void loginAndLogoutInJenkinsIsReflectedInKeycloak() {
        final KeycloakUtils.User bob =
                new KeycloakUtils.User(userBobKeycloakId, "bob", "bob@jenkins-ath.test", "Bob", "Smith");
        final KeycloakUtils.User john =
                new KeycloakUtils.User(userJohnKeycloakId, "john", "john@jenkins-ath.test", "John", "Smith");

        jenkins.open();
        getLogin().click();
        keycloakUtils.login(bob.getUserName());
        assertLoggedUser(bob, "jenkinsAdmin");

        jenkins.logout();
        jenkins.open();
        assertLoggedOut();

        // As the option Logout from Openid Provider is set to true, a logout from Jenkins means a logout from Keycloak
        // so the login page will appear again
        jenkins.open();
        getLogin().click();
        keycloakUtils.login(john.getUserName());
        assertLoggedUser(john, "jenkinsRead");
    }

    /**
     * Test the login and logout in the OIDC provider and how it is propagated to Jenkins
     * - A login in the provider will be detected by the plugin so Jenkins won't prompt the login form
     * - A logout in the provider will redirect to the Jenkins logout action (configured in keycloak) so the user
     *   will log out from Jenkins as well
     */
    @Test
    public void loginAndLogoutInKeycloakIsReflectedInJenkins() throws Exception {
        final KeycloakUtils.User bob =
                new KeycloakUtils.User(userBobKeycloakId, "bob", "bob@jenkins-ath.test", "Bob", "Smith");
        final KeycloakUtils.User john =
                new KeycloakUtils.User(userJohnKeycloakId, "john", "john@jenkins-ath.test", "John", "Smith");
        final String loginUrl = String.format("%s/realms/%s/account", keycloak.getAuthServerUrl(), REALM);

        keycloakUtils.open(new URL(loginUrl));
        keycloakUtils.login(bob.getUserName());
        jenkins.open();
        getLogin().click(); // won't request a login, but log in directly with user from

        assertLoggedUser(bob, "jenkinsAdmin");

        keycloakUtils.logout(keycloak.getAuthServerUrl(), REALM, bob);
        // XXX OIDC backchannel logout is not implemented so logout from Jenkins
        jenkins.open();
        jenkins.logout();
        assertLoggedOut();

        // Once logged out, we can change the user
        keycloakUtils.open(new URL(loginUrl));
        keycloakUtils.login(john.getUserName());
        jenkins.open();
        getLogin().click();
        assertLoggedUser(john, "jenkinsRead");
    }

    private WebElement getLogin() {
        return getElement(by.href("/securityRealm/commenceLogin?from=%2F"));
    }

    private void assertLoggedOut() {
        assertNull("User has logged out from Jenkins", jenkins.getCurrentUser().id());

        assertThrows(
                "User has logged out from keycloak",
                NoSuchElementException.class,
                () -> keycloakUtils.getCurrentUser(keycloak.getAuthServerUrl(), REALM));
    }

    private void assertLoggedUser(KeycloakUtils.User expectedUser, String roleToCheck) {
        assertThat("User has logged in Jenkins", jenkins.getCurrentUser().id(), is(expectedUser.getId()));

        WhoAmI whoAmI = new WhoAmI(jenkins);
        whoAmI.open();
        JSONObject jsonData = new JSONObject(whoAmI.getJson().toString());
        assertThat("User is the expected one", jsonData.getString("name"), is(expectedUser.getId()));
        assertThat(
                "User has the expected roles inherited from keycloak",
                roleToCheck,
                is(in(jsonData.getJSONArray("authorities").toList())));
    }
}
