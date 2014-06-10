//
// +-----------------------------------------------------+
// |              =========================              |
// |              !  W  A  R  N  I  N  G  !              |
// |              =========================              |
// |                                                     |
// | This file is  N O T   P A R T  of the jenkins       |
// | acceptance test harness project's source code!      |
// |                                                     |
// | This file is only used for testing purposes w.r.t   |
// | the task scanner plugin test.                       |
// |                                                     |
// +-----------------------------------------------------+
//

package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;

/**
 * SecurityRealm for ldap plugin.
 *
 * @author Michael Prankl
 */
@Describable("LDAP")
public class TSRLdapSecurityRealm extends SecurityRealm {

    private final Control ldapServer = control("server");
    private final Control advanced = control("advanced-button");
    private final Control rootDn = control("rootDN");
    private final Control managerDn = control("managerDN");
    private final Control managerPassword = control("managerPasswordSecret"/* >= 1.9*/, "managerPassword");
    private final Control userSearchBase = control("userSearchBase");
    private final Control userSearchFilter = control("userSearch");
    private final Control groupSearchBase = control("groupSearchBase");
    private final Control groupSearchFilter = control("groupSearchFilter");
    private final Control groupMembershipFilter = control("groupMembershipFilter");
    private final Control disableLdapEmailResolver = control("disableMailAddressResolver");
    private final Control enableCache = control("cache");
    /**
     * since version 1.8
     */
    private final Control displayNameAttributeName = control("displayNameAttributeName");
    /**
     * since version 1.8
     */
    private final Control mailAddressAttributeName = control("mailAddressAttributeName");

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
        // TODO change this when plugin test is comfortable with changes in ldap@1.10
        if (ldapDetails.getGroupMembershipFilter() != null) {
            groupMembershipFilter.set(ldapDetails.getGroupMembershipFilter());
        }
        disableLdapEmailResolver.check(ldapDetails.isDisableLdapEmailResolver());
        if (ldapDetails.isEnableCache()) {
            enableCache.check(true);
            control("cache/size[" + ldapDetails.getCacheSize() + "]").check(true);
            control("cache/ttl[" + ldapDetails.getCacheTTL() + "]").check(true);
        }
        if (ldapDetails.getDisplayNameAttributeName() != null) {
            displayNameAttributeName.set(ldapDetails.getDisplayNameAttributeName());
        }
        if (ldapDetails.getMailAddressAttributeName() != null) {
            mailAddressAttributeName.set(ldapDetails.getMailAddressAttributeName());
        }
    }
}
