package org.jenkinsci.test.acceptance.po;

import org.junit.Assert;

import static org.jenkinsci.test.acceptance.Matchers.hasInvalidLoginInformation;
import static org.jenkinsci.test.acceptance.Matchers.loggedInAs;

/**
 * Page object for login page.
 *
 * @author Michael Prankl
 */
public class Login extends PageObject {

    private Control cUser = control("/j_username");
    private Control cPassword = control("/j_password");
    private Control cLogin = control("/Submit");

    public Login(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("login"));
    }

    /**
     * Login assuming "path" form element availability.<br>
     * Paths are usually available when using the default Jenkins controller.<br>
     * (Available thanks to pre-installed form-element-path plugin.)
     */
    public Login doLogin(String user, String password){
        cUser.set(user);
        cPassword.set(password);
        cLogin.click();
        return this;
    }

    /**
     * Login assuming "path" form element unavailability.<br>
     * Paths are usually unavailable when using the "existing" Jenkins controller.<br>
     * (Unavailable despite pre-installed form-element-path plugin.)
     */
    public Login doLoginDespiteNoPaths(String user, String password){
        driver.findElement(by.name("j_username")).sendKeys(user);
        driver.findElement(by.name("j_password")).sendKeys(password);
        clickButton("log in");
        return this;
    }

    /**
     * Login for a common case where the user name and the password are the same
     */
    public Login doLogin(String user) {
        return doLogin(user,user);
    }

    public Login doLogin(User user) {
        return doLogin(user.fullName());
    }

    public Login doSuccessfulLogin(String user, String password) {
        this.doLogin(user, password);
        Assert.assertThat(this, loggedInAs(user));
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
        Assert.assertThat(this, hasInvalidLoginInformation());
        return this;
    }

    public Login doFailedLogin(String user) {
        return this.doFailedLogin(user, user);
    }

    public Login doFailedLogin(User user) {
        return this.doFailedLogin(user.fullName());
    }

}
