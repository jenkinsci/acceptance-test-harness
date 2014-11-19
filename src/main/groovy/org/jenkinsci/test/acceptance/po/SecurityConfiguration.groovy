package org.jenkinsci.test.acceptance.po

import javax.inject.Inject

/**
 * Page object for the security configuration page.
 *
 * The page object did not contains all possible form fields!
 *
 * @author christian.fritz
 */
class SecurityConfiguration extends Page {

    static url = "configureSecurity"
    static at = { title == "Configure Global Security [Jenkins]" }
    static content = {
        useSecurity { $("input[path='/useSecurity']") }
        avoidCSRF { $("input[path='/hudson-security-csrf-GlobalCrumbIssuerConfiguration/csrf']") }
        disableRememberMe { $("input[path='/useSecurity/disableRememberMe']") }
        submit { $("span.submit-button button") }
    }

    @Inject Jenkins jenkins;

    def useRealm(Class<? extends SecurityRealm> type) {
        new GlobalSecurityConfig(jenkins).useRealm(type)
    }

    def useAuthorizationStrategy(Class<? extends AuthorizationStrategy> type) {
        new GlobalSecurityConfig(jenkins).useAuthorizationStrategy(type)
    }
}
