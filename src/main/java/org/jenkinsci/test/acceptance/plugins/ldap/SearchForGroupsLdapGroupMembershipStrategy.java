package org.jenkinsci.test.acceptance.plugins.ldap;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Michael Prankl
 */
@Describable({"Search for groups containing user", "Search for LDAP groups containing user"})
public class SearchForGroupsLdapGroupMembershipStrategy extends LdapGroupMembershipStrategy {

    private Control groupMembershipFilter = control("filter");

    public SearchForGroupsLdapGroupMembershipStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    @Override
    public void configure(String strategyParam) {
        if (strategyParam != null) {
            groupMembershipFilter.set(strategyParam);
        }
    }

}
