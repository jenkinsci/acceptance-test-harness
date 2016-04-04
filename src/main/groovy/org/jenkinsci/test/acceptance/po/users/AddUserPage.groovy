package org.jenkinsci.test.acceptance.po.users

import org.jenkinsci.test.acceptance.po.Page
import org.kohsuke.randname.RandomNameGenerator

/**
 * Geb Page object for adding a new user.
 *
 * @author christian.fritz
 */
class AddUserPage extends Page {

    static url = "securityRealm/addUser"
    static at = { title == "Create User [Jenkins]" }
    static content = {
        username { $("input[path='/username']") }
        password { $("input[path='/password1']") }
        confirmPassword { $("input[path='/password2']") }
        fullName { $("input[path='/fullname']") }
        eMailAddress { $("input[path='/email']") }
        signUp(to: UserListPage) { $("span.submit-button button") }
    }
    private static RandomNameGenerator randomNameGenerator = new RandomNameGenerator();

    private static def randName() {
        randomNameGenerator.next();
    }

    /**
     * Fill out the user info
     *
     * @return the username of the created user
     */
    def fillUserInfo() {
        def name = randName();
        def pwd = randName();

        username.value(name);
        password.value(pwd);
        confirmPassword.value(pwd);
        fullName.value(name);
        eMailAddress.value(name + "@example.com");
        name
    }
}
