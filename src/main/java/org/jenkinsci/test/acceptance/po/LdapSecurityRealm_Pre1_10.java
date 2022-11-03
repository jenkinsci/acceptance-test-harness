package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;

/**
 * Ldap security realm for ldap plugin prior to version 1.10.
 *
 * @author Michael Prankl
 * @deprecated only use this if you are really testing against a ldap plugin version older than 1.10
 */
@Deprecated
@Describable("LDAP")
public class LdapSecurityRealm_Pre1_10 extends LdapSecurityRealm {

    public LdapSecurityRealm_Pre1_10(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    @Override
    protected void configureGroupMembership(LdapDetails ldapDetails) {
        groupMembershipFilter.set(ldapDetails.getGroupMembershipFilter());
    }
}
