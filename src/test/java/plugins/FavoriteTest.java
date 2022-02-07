package plugins;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

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

        waitFor(by.id("fav_my-project")).click();
        // ensure the project is now a favourite
        waitFor(by.css(".icon-fav-active"));

        final User user = new User(jenkins, USER);
        jenkins.visit(user.getConfigUrl().toString());
        waitFor(by.id("favorites"));
        waitFor(by.id("fav_my-project")).click();
        // ensure the project is no longer a favourite
        waitFor(by.css(".icon-fav-inactive"));

        jenkins.visit(user.getConfigUrl().toString());
        waitFor(driver).until(Matchers.hasContent("Favorites"));
        assertFalse(findIfNotVisible(by.id("favorites")).isDisplayed());
    }
}
