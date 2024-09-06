package org.jenkinsci.test.acceptance.po;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasInvalidLoginInformation;
import static org.jenkinsci.test.acceptance.Matchers.loggedInAs;

import java.time.Duration;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for login page.
 *
 * @author Michael Prankl
 */
public class Login extends PageObject {

    private Control cUser = control(by.name("j_username"));
    private Control cPassword = control(by.name("j_password"));
    private Control cLogin = control(by.name("Submit"));

    public Login(Jenkins jenkins) {
        super(jenkins, jenkins.url("login"));
    }

    /**
     * Login assuming "path" form element availability.<br>
     * Paths are usually available when using the default Jenkins controller.<br>
     * (Available thanks to pre-installed form-element-path plugin.)
     */
    public Login doLogin(String user, String password) {
        cUser.set(user);
        cPassword.set(password);
        // for some reason submit it just bogus...
        cLogin.clickAndWaitToBecomeStale();
        return this;
    }

    /**
     * @deprecated use {@link #doLogin(String, String)} instead. It doesn't require form-element-path.
     */
    @Deprecated
    public Login doLoginDespiteNoPaths(String user, String password) {
        return doLogin(user, password);
    }

    /**
     * Login for a common case where the user name and the password are the same
     */
    public Login doLogin(String user) {
        return doLogin(user, user);
    }

    public Login doLogin(User user) {
        return doLogin(user.fullName());
    }

    public Login doSuccessfulLogin(String user, String password) {
        this.doLogin(user, password);
        waitFor().withTimeout(Duration.ofSeconds(30)).until(() -> {
            assertThat(this, not(hasInvalidLoginInformation())); // login hasn't failed
            return ExpectedConditions.visibilityOfElementLocated(by.id("jenkins")); // redirect has happened
        });
        assertThat(this, loggedInAs(user));
        return this;
    }

    public Login doSuccessfulLogin(String user) {
        return this.doSuccessfulLogin(user, user);
    }

    public Login doSuccessfulLogin(User user) {
        return this.doSuccessfulLogin(user.fullName());
    }

    public Login doFailedLogin(String user, String password) {
        this.doLogin(user, password);
        assertThat(this, hasInvalidLoginInformation());
        return this;
    }

    public Login doFailedLogin(String user) {
        return this.doFailedLogin(user, user);
    }

    public Login doFailedLogin(User user) {
        return this.doFailedLogin(user.fullName());
    }
}
