package org.jenkinsci.test.acceptance.plugins.mock_security_realm;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.SecurityRealm;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Mock Security Realm")
public class MockSecurityRealm extends SecurityRealm {
    private final Control data = control("data");

    public MockSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    /**
     * Sets up the data.
     *
     * @param accounts
     *      Each account should be an user name optionally followed by group names.
     *      The password is always the same as the user name.
     */
    public void configure(String... accounts) {
        data.set(StringUtils.join(accounts, "\n"));
    }
}
