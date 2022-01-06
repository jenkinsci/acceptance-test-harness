package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.Test;

@WithPlugins({"favorite", "mock-security-realm"})
public class FavoriteTest extends AbstractJUnitTest {
    public static final String USER = "dev";

    @Test
    public void shouldBeAbleToFavoriteAJob() throws Exception {
        jenkins.jobs.create(FreeStyleJob.class, "my-project");

        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        MockSecurityRealm realm = security.useRealm(MockSecurityRealm.class);
        realm.configure(USER);
        security.save();

        jenkins.login().doLogin(USER);

        jenkins.open();
        waitFor(by.id("fav_my-project")).click();
        jenkins.visit("/user/"+USER+"/configure");
        waitFor(by.id("fav_my-project")).click();
    }
}
