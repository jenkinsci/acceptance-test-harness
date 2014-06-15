package org.jenkinsci.test.acceptance.po

import org.kohsuke.randname.RandomNameGenerator

/**
 *
 * @author christian.fritz
 */
class AddUserPage extends Page {

    static url = "securityRealm/addUser"
    static at = { title == "Sign up [Jenkins]" }
    static content = {
        username { $("input[path='/username']") }
        password { $("input[path='/password1']") }
        confirmPassword { $("input[path='/password2']") }
        fullName { $("input[path='/fullname']") }
        eMailAddress { $("input[path='/email']") }
        signUp(to: UserListPage) { $("span.submit-button button") }
    }
    private static RandomNameGenerator randomNameGenerator = new RandomNameGenerator();

    static def randName() {
        randomNameGenerator.next();
    }

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
