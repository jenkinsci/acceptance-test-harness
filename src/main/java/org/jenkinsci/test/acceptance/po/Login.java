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

    public Login doLogin(String user, String password){
        cUser.set(user);
        cPassword.set(password);
        cLogin.click();
        return this;
    }

}
