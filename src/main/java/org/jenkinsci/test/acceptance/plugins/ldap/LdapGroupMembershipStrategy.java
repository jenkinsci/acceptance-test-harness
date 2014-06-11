package org.jenkinsci.test.acceptance.plugins.ldap;

import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Use @Describable to register an implementation.
 *
 * @author Michael Prankl
 * @since ldap version 1.10
 */
public abstract class LdapGroupMembershipStrategy extends PageAreaImpl {

    protected LdapGroupMembershipStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    public abstract void configure(String strategyParam);
}
