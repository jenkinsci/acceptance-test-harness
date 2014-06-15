package groovy.core

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.po.AddUserPage
import org.jenkinsci.test.acceptance.po.SecurityConfiguration
import org.jenkinsci.test.acceptance.po.UserListPage

/**
 *
 * @author christian.fritz
 */
class InternalUsersTest extends GebSpec {

    def "Create, update delete user"() {
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
    }
}
