package groovy.core

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.po.SecurityConfiguration

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
    }
}
