package org.jenkinsci.test.acceptance.po

/**
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

        securityRealm {
            [
                    delegateContainer: $("input[path='/useSecurity/realm[0]']"),
                    jenkinsDB        : $("input[path='/useSecurity/realm[1]']"),
                    ldap             : $("input[path='/useSecurity/realm[2]']"),
                    unix             : $("input[path='/useSecurity/realm[3]']"),
            ]
        }

        authorization {
            [
                    loggedInCanAll    : $("input[path='/useSecurity/authorization[0]']"),
                    anyoneCanAll      : $("input[path='/useSecurity/authorization[1]']"),
                    legacyMode        : $("input[path='/useSecurity/authorization[2]']"),
                    matrixBased       : $("input[path='/useSecurity/authorization[3]']"),
                    projectMatrixBased: $("input[path='/useSecurity/authorization[4]']"),
            ]
        }
    }
}
