package plugins;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ownership.OwnershipAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.User;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.google.inject.Inject;

@WithPlugins("ownership")
public class OwnershipPluginTest extends AbstractJUnitTest {

    @Inject
    private SlaveController slaves;

    @Test @Since("1.509")
    public void explicitly_set_ownership() throws Exception {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        JenkinsDatabaseSecurityRealm realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
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

    private void own(ContainerPageObject item, User user) {
        item.action(OwnershipAction.class).setPrimaryOwner(user);
    }

    private Matcher<ContainerPageObject> ownedBy(final User user) {
        final Matcher<WebDriver> inner = Matchers.hasContent("Primary owner: " + user);
        return new Matcher<ContainerPageObject>("Item owned by " + user) {
            @Override public boolean matchesSafely(ContainerPageObject item) {
                item.open();
                return inner.matchesSafely(driver);
            }

            @Override public void describeMismatchSafely(ContainerPageObject item, Description desc) {
                item.open();
                inner.describeMismatchSafely(driver, desc);
            }
        };
    }
}
