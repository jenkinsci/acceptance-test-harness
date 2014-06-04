package org.jenkinsci.test.acceptance.po;

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

}
