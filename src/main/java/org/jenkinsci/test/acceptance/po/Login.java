package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.TimeoutException;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.jenkinsci.test.acceptance.Matchers.hasLoggedInUser;
import static org.junit.Assert.assertTrue;

/**
 * Page object for login page.
 *
 * @author Michael Prankl
 */
public class Login extends PageObject {

    private String loginUser;

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
        this.loginUser = user;

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

    public Login shouldSucceed() {
        return this.shouldSucceed(10);
    }

    public Login shouldSucceed(int timeoutInSeconds) {
        if (this.loginUser == null) {
            throw new RuntimeException("Checking login success is not possible without login in first");
        }

        try {
            waitFor(getJenkins(), hasLoggedInUser(this.loginUser), timeoutInSeconds);
        } catch (TimeoutException ex) {
            assertTrue(this.loginUser + " user is not logged in", false);
        }

        return this;
    }

    public Login shouldFail() {
        return this.shouldFail(10);
    }

    public Login shouldFail(int timeoutInSeconds) {
        if (this.loginUser == null) {
            throw new RuntimeException("Checking login failure is not possible without login in first");
        }

        try {
            waitFor(driver, hasContent("Invalid login information. Please try again."), timeoutInSeconds);
        } catch (TimeoutException ex) {
            assertTrue("Login did not fail", false);
        }

        return this;
    }

}
