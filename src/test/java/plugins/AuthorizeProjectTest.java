package plugins;

import com.google.common.base.Joiner;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.authorize_project.ProjectDefaultBuildAccessControl;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Tests authorize project plugin
 */
@WithPlugins({"authorize-project"})
public class AuthorizeProjectTest extends AbstractJUnitTest {

    private static final String USER1 = "user1";
    private static final String USER2 = "user2";

    private static final String STARTED_BY_USER2 = "Started by user " + USER2;
    private static final String RUNNING_ANONYMOUS = "Running as anonymous";

    @Before
    public void setup() {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();

        this.setupUsers(security, USER1, USER2);

        security.save();
    }

    @Test
    @WithPlugins("mock-security-realm")
    public void testProjectRunByUser() {
        jenkins.login().doSuccessfulLogin(USER2);

        final FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.save();
        Build b = job.startBuild().shouldSucceed();

        String consoleOutput = b.getConsole();
        assertThat(consoleOutput, containsString(STARTED_BY_USER2));
        assertThat(consoleOutput, not(containsString(RUNNING_ANONYMOUS)));

        this.authorizeUserToLaunchProject(USER1);

        b = job.startBuild().shouldSucceed();
        consoleOutput = b.getConsole();
        assertThat(consoleOutput, containsString(STARTED_BY_USER2));
        // Running as anonymous is displayed due to permissions but the plugin performs its job
        assertThat(consoleOutput, containsString(RUNNING_ANONYMOUS));
    }

    private void setupUsers(final GlobalSecurityConfig security, final String... users) {
        final MockSecurityRealm realm = security.useRealm(MockSecurityRealm.class);
        realm.configure(Joiner.on("\n").join(users));
    }

    private void authorizeUserToLaunchProject(final String user) {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();

        final ProjectDefaultBuildAccessControl control = security.addBuildAccessControl(ProjectDefaultBuildAccessControl.class);
        control.runAsSpecificUser(user);

        security.save();
    }

}
