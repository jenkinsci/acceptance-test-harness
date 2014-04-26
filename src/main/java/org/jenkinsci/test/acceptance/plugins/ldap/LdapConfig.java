package org.jenkinsci.test.acceptance.plugins.ldap;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.SecurityConfig;

/**
 * PageArea containing input fields for ldap server configuration.
 *
 * @author Michael Prankl
 */
public class LdapConfig extends PageArea {

    private final Control cRealm = control("realm[2]");
    private final Control cLdapServer = control("realm[2]/server");
    private final Control cAdvanced = control("realm[2]/advanced-button");
    private final Control cRootDn = control("realm[2]/rootDN");
    private final Control cManagerDn = control("realm[2]/managerDN");
    private final Control cManagerPassword = control("realm[2]/managerPassword");
    private final Control cUserSearchBase = control("realm[2]/userSearchBase");
    private final Control cUserSearchFilter = control("realm[2]/userSearch");

    public LdapConfig(SecurityConfig securityConfig) {
        super(securityConfig, "/useSecurity");
    }

    /**
     * Fills the input fields for ldap access control.
     */
    public void enterLdapDetails(LdapDetails ldapDetails) {
        cRealm.check(true);
        cLdapServer.set(ldapDetails.getHostWithPort());
        cAdvanced.click();
        cRootDn.set(ldapDetails.getRootDn());
        cManagerDn.set(ldapDetails.getManagerDn());
        cManagerPassword.set(ldapDetails.getManagerPassword());
        if (ldapDetails.getUserSearchBase() != null) {
            cUserSearchBase.set(ldapDetails.getUserSearchBase());
        }
        if (ldapDetails.getUserSearchFilter() != null) {
            cUserSearchFilter.set(ldapDetails.getUserSearchFilter());
        }
    }
}
