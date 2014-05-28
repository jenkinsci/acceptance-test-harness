package org.jenkinsci.test.acceptance.plugins.ldap;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;

/**
 * @author Michael Prankl
 */
@Describable("Parse user attribute for list of groups")
public class ParseUserAttributeLdapGroupMembershipStrategy extends LdapGroupMembershipStrategy {

    private Control groupMembershipAttribute = control("attribute");

    public ParseUserAttributeLdapGroupMembershipStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    @Override
    public void configure(String strategyParam) {
        if (strategyParam != null) {
            groupMembershipAttribute.set(strategyParam);
        }
    }
}
