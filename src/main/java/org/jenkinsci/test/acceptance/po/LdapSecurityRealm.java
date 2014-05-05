package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;

/**
 * @author Michael Prankl
 */
@Describable("LDAP")
public class LdapSecurityRealm extends SecurityRealm {

    private final Control ldapServer = control("server");
    private final Control advanced = control("advanced-button");
    private final Control rootDn = control("rootDN");
    private final Control managerDn = control("managerDN");
    private final Control managerPassword = control("managerPassword");
    private final Control userSearchBase = control("userSearchBase");
    private final Control userSearchFilter = control("userSearch");
    private final Control groupSearchBase = control("groupSearchBase");
    private final Control groupSearchFilter = control("groupSearchFilter");
    private final Control groupMembershipFilter = control("groupMembershipFilter");
    private final Control disableLdapEmailResolver = control("disableMailAddressResolver");
    private final Control enableCache = control("cache");

    public LdapSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    /**
     * Fills the input fields for ldap access control.
     */
    public void configure(LdapDetails ldapDetails) {
        ldapServer.set(ldapDetails.getHostWithPort());
        advanced.click();
        rootDn.set(ldapDetails.getRootDn());
        managerDn.set(ldapDetails.getManagerDn());
        managerPassword.set(ldapDetails.getManagerPassword());
        userSearchBase.set(ldapDetails.getUserSearchBase());
        userSearchFilter.set(ldapDetails.getUserSearchFilter());
        groupSearchBase.set(ldapDetails.getGroupSearchBase());
        groupSearchFilter.set(ldapDetails.getGroupSearchFilter());
        groupMembershipFilter.set(ldapDetails.getGroupMembershipFilter());
        disableLdapEmailResolver.check(ldapDetails.isDisableLdapEmailResolver());
        if (ldapDetails.isEnableCache()) {
            enableCache.check(true);
            control("cache/size[" + ldapDetails.getCacheSize() + "]").check(true);
            control("cache/ttl[" + ldapDetails.getCacheTTL() + "]").check(true);
        }
    }
}
