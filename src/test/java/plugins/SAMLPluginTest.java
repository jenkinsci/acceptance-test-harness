package plugins;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.DockerImage;
import org.jenkinsci.test.acceptance.docker.fixtures.SAMLContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow;
import org.jenkinsci.test.acceptance.plugins.saml.SamlSecurityRealm;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.utils.process.CommandBuilder;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow.OVERALL_READ;
import static org.junit.Assert.assertThat;

public class SAMLPluginTest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<SAMLContainer> samlContainer;

    private static final String SERVICE_PROVIDER_ID = "jenkins-dev";

    @Test @WithDocker
    @WithPlugins({"saml", "matrix-auth"})
    public void authentication() throws IOException, InterruptedException {
        jenkins.open(); // navigate to root
        String rootUrl = jenkins.getCurrentUrl();
        DockerImage.Starter<SAMLContainer> starter = samlContainer.starter();
        File users = new File("src/test/resources/saml_plugin/users.php");
        File config = new File("src/test/resources/saml_plugin/config.php");
        starter.withOptions(new CommandBuilder(
                "-e", "SIMPLESAMLPHP_SP_ENTITY_ID=" + SERVICE_PROVIDER_ID, // service provider ID
                "-e", "SIMPLESAMLPHP_SP_ASSERTION_CONSUMER_SERVICE=" + rootUrl + "securityRealm/finishLogin", // login back URL
                "-e", "SIMPLESAMLPHP_SP_SINGLE_LOGOUT_SERVICE=" + rootUrl + "logout", // unused
                "-v", users.getAbsolutePath() + ":/var/www/simplesamlphp/config/authsources.php", // users info
                "-v", config.getAbsolutePath() + ":/var/www/simplesamlphp/config/config.php" // config info
        ));
        SAMLContainer samlServer = starter.start();
        System.out.println("============ SAML Server: " + samlServer.host() + ":" + samlServer.port());

        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();

        // Authentication
        SamlSecurityRealm realm = sc.useRealm(SamlSecurityRealm.class);
        realm.setUserNameAttribute("uid");
        realm.setGroupsAttribute("eduPersonAffiliation");
        realm.setEmailAttribute("email");
        realm.setDisplayNameAttribute("displayName");
        realm.advancedConfig();
        realm.setSpEntityIdAttribute(SERVICE_PROVIDER_ID);
        // get saml metadata from IdP
        URL metadata = new URL("http://" + samlServer.host() + ":" + samlServer.port() + "/simplesaml/saml2/idp/metadata.php");
        URLConnection connection = metadata.openConnection();
        realm.setSamlMetadata(IOUtils.toString(connection.getInputStream()));

        // encryption
        realm.encryptionConfig();
        realm.setKeyStorePath(new File("src/test/resources/saml_plugin/saml-key.jks").getAbsolutePath());
        realm.setKeyStorePassword("changeit");
        realm.setPrivateKeyPassword("changeit");


        // Authorization
        MatrixAuthorizationStrategy mas = sc.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);
        // groups coming from the SAML IdP
        MatrixRow group1 = mas.addUser("group1"); // admins
        group1.admin();
        MatrixRow group2 = mas.addUser("group2"); // readers
        group2.on(OVERALL_READ);
        sc.save(); // after save the user has no login, so automatic redirect to the auth service

        waitFor().withTimeout(10, TimeUnit.SECONDS).until(() -> hasContent("Enter your username and password")); // SAML service login page

        // SAML server login
        find(by.id("username")).sendKeys("user1");
        find(by.id("password")).sendKeys("user1pass");
        find(by.button("Login")).click();

        waitFor().withTimeout(5, TimeUnit.SECONDS).until(() -> hasContent("User 1").matchesSafely(driver)); // wait for the login to propagate
        assertThat(jenkins.getCurrentUser().id(), equalTo("user1"));

        MatcherAssert.assertThat(driver, hasContent("Manage Jenkins")); // user1 - group1 (admins)
    }
}
