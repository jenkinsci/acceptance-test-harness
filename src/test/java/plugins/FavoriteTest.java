package plugins;

import static org.junit.Assert.assertFalse;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Test;

@WithPlugins({"favorite"})
public class FavoriteTest extends AbstractJUnitTest {
    public static final String USER = "dev";

    @Test
    public void shouldBeAbleToFavoriteAJob() throws Exception {
        jenkins.jobs.create(FreeStyleJob.class, "my-project");

        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        JenkinsDatabaseSecurityRealm realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
        realm.allowUsersToSignUp(true);
        security.save();

        realm.signup(USER);
        jenkins.login().doLogin(USER);

        waitFor(by.css("a.favorite-toggle[data-fullname='my-project'][data-fav='false']")).click();
        // ensure the project is now a favourite
        waitFor(by.css(".icon-fav-active:not(.jenkins-hidden)"));

        final User user = new User(jenkins, USER);
        jenkins.visit(user.url("favorites").toString());
        // will fail if the project has not been favorited
        waitFor(by.css("fup-favorite-dev"));
        waitFor(by.css("a.favorite-toggle[data-fullname='my-project'][data-fav='true']")).click();
        // ensure the project is no longer a favourite
        waitFor(by.css(".icon-fav-inactive:not(.jenkins-hidden)"));

        jenkins.visit(user.url("favorites").toString());
        waitFor(driver).until(Matchers.hasContent("Favorites"));
        assertFalse(findIfNotVisible(by.id("favorites")).isDisplayed());
    }
}
