package groovy.core

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.po.SecurityConfiguration
import org.jenkinsci.test.acceptance.po.users.AddUserPage
import org.jenkinsci.test.acceptance.po.users.ConfigureUserPage
import org.jenkinsci.test.acceptance.po.users.DeleteUserPage
import org.jenkinsci.test.acceptance.po.users.UserListPage

/**
 * Test for the internal user dictionary of jenkins.
 *
 * @author christian.fritz
 */
class InternalUsersTest extends GebSpec {

    def "Create, update and delete user"() {
        given: "Use the internal user authentification"
        to SecurityConfiguration
        useSecurity.value(true)
        securityRealm.jenkinsDB.click()
        submit.click()

        when: "create a new user"
        to AddUserPage
        def createdUserName = fillUserInfo()
        signUp.click()

        then: "should be on user list"
        at UserListPage
        assert userNames.size() == 1
        def fullName = fullNames.get(0)

        when: "change the full name for the user"
        configure[createdUserName].click()
        at ConfigureUserPage
        def newFullName = changeFullName()
        save.click()

        then: "the user should exist but with the new full name"
        to UserListPage
        assert userNames.size() == 1
        assert fullNames.get(0) != fullName
        assert fullNames.get(0) == newFullName

        when: "delete the first user"
        delete[createdUserName].click();
        at DeleteUserPage
        yes.click()

        then: "there should be any user"
        at UserListPage
        assert userNames.size() == 0
    }
}
