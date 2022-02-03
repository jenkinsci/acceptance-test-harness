package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static org.junit.Assert.assertTrue;

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
        WebElement project_fav = waitFor(by.id("fav_my-project"));
        project_fav.click();
        waitFor(project_fav).withTimeout(Duration.ofSeconds(5)).until(Control::isStale);

        final User user = new User(jenkins, USER);
        jenkins.visit(user.getConfigUrl().toString());
        assertTrue(waitFor(by.id("favorites")).isDisplayed());
        WebElement project_unfav = waitFor(by.id("fav_my-project"));
        project_unfav.click();
        waitFor(project_unfav).withTimeout(Duration.ofSeconds(5)).until(Control::isStale);

        jenkins.visit(user.getConfigUrl().toString());
        assertTrue(waitFor(by.id("favorites")).getText().isEmpty());
    }
}
