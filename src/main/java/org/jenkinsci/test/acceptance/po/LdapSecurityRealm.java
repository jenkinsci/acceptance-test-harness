package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapGroupMembershipStrategy;
import org.openqa.selenium.WebElement;

/**
 * SecurityRealm for ldap plugin.
 *
 * @author Michael Prankl
 * @see org.jenkinsci.test.acceptance.po.LdapSecurityRealm_Pre1_10 if you want to test versions of the plugin < 1.10
 */
@Describable("LDAP")
public class LdapSecurityRealm<T extends LdapGroupMembershipStrategy> extends SecurityRealm {

    private GlobalSecurityConfig context;

    protected final Control ldapServer = control("server");
    protected final Control advanced = control("advanced-button");
    protected final Control rootDn = control("rootDN");
    protected final Control managerDn = control("managerDN");
    protected final Control managerPassword = control("managerPasswordSecret"/* >= 1.9*/, "managerPassword");
    protected final Control userSearchBase = control("userSearchBase");
    protected final Control userSearchFilter = control("userSearch");
    protected final Control groupSearchBase = control("groupSearchBase");
    protected final Control groupSearchFilter = control("groupSearchFilter");
    /**
     * only available prior ldap plugin version 1.10
     */
    protected final Control groupMembershipFilter = control("groupMembershipFilter");
    protected final Control disableLdapEmailResolver = control("disableMailAddressResolver");
    protected final Control enableCache = control("cache");
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
        this.context = context;
    }

    private T useGroupMembershipStrategy(Class<T> type) {
        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override
            protected WebElement find(String caption) {
                return getElement(by.radioButton(caption));
            }
        });
        radio.click();
        return newInstance(type, this.context, radio.getAttribute("path"));
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
        configureGroupMembership(ldapDetails);
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

    /**
     * Subclasses can override this to handle group membership differently.
     */
    protected void configureGroupMembership(LdapDetails ldapDetails) {
        if (ldapDetails.getGroupMembershipStrategy() != null) {
            T groupMembershipStrategy = useGroupMembershipStrategy(ldapDetails.getGroupMembershipStrategy());
            groupMembershipStrategy.configure(ldapDetails.getGroupMembershipStrategyParam());
        }
    }

}
