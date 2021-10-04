package plugins;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ownership.OwnershipAction;
import org.jenkinsci.test.acceptance.plugins.ownership.OwnershipGlobalConfig;
import org.jenkinsci.test.acceptance.po.*;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

@WithPlugins({"ownership", "cloudbees-folder"})
public class OwnershipPluginTest extends AbstractJUnitTest {

    @Inject
    private SlaveController slaves;

    @Test
    @Since("1.509")
    public void explicitly_set_ownership() throws Exception {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        JenkinsDatabaseSecurityRealm realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
        realm.allowUsersToSignUp(true);
        security.save();

        User user = realm.signup("jenkins-acceptance-tests-user");

        Slave slave = slaves.install(jenkins).get();
        own(slave, user);

        FreeStyleJob job = jenkins.jobs.create();
        job.save();
        own(job, user);

        assertThat(slave, ownedBy(user));
        assertThat(job, ownedBy(user));
    }

    @Test
    @Since("1.509")
    public void implicitly_set_job_ownership() {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        JenkinsDatabaseSecurityRealm realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
        realm.allowUsersToSignUp(true);
        security.save();

        final JenkinsConfig globalConfig = jenkins.getConfigPage();
        globalConfig.configure();
        new OwnershipGlobalConfig(globalConfig).setImplicitJobOwnership();
        globalConfig.save();

        User user = realm.signup("jenkins-acceptance-tests-user");
        jenkins.login().doLogin(user);

        FreeStyleJob job = jenkins.jobs.create();
        job.save();

        elasticSleep(1000);
        assertThat(job, ownedBy(user));
    }

    @Test
    @Since("1.509") @Issue("JENKINS-24370")
    public void correct_redirect_after_save() throws Exception {
        JenkinsConfig cp = jenkins.getConfigPage();
        cp.configure();
        cp.setJenkinsUrl("http://www.google.com");
        cp.save();

        Slave slave = slaves.install(jenkins).get();
        slave.visit("ownership/manage-owners");
        clickButton("Save");
        assertThat(currentUrl(), equalTo(slave.url));

        FreeStyleJob job = jenkins.jobs.create();
        job.visit("ownership/manage-owners");
        clickButton("Save");
        assertThat(currentUrl(), equalTo(job.url));

        job.visit("ownership/configure-project-specifics");
        clickButton("Save");
        assertThat(currentUrl(), equalTo(job.url));

        job.visit("ownership/configure-project-specifics");
        clickButton("Restore default settings...");
        assertThat(currentUrl(), equalTo(job.url));
    }

    private URL currentUrl() throws MalformedURLException, UnsupportedEncodingException {
        return new URL(URLDecoder.decode(driver.getCurrentUrl(), "UTF-8"));
    }

    private void own(ContainerPageObject item, User user) {
        item.action(OwnershipAction.class).setPrimaryOwner(user);
    }

    private Matcher<ContainerPageObject> ownedBy(final User user) {
        final Matcher<WebDriver> inner = Matchers.hasContent(Pattern.compile("(Primary owner: |Owner\\n|Primary\n|\n)" + user.id()));
        return new Matcher<ContainerPageObject>("Item owned by " + user.id()) {
            @Override
            public boolean matchesSafely(ContainerPageObject item) {
                item.open();
                return inner.matchesSafely(driver);
            }

            @Override
            public void describeMismatchSafely(ContainerPageObject item, Description desc) {
                item.open();
                inner.describeMismatchSafely(driver, desc);
            }
        };
    }
}
