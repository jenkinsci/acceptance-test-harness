package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapEnvironmentVariable;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapGroupMembershipStrategy;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

    protected final Control ldapServer = control("configurations/server" /* >= 1.16 */, "server");
    protected final Control advancedServer = control("configurations/advanced-button" /* >= 1.16 */, "advanced-button");
    protected final Control rootDn = control("configurations/rootDN" /* >= 1.16 */, "rootDN");
    protected final Control managerDn = control("configurations/managerDN" /* >= 1.16 */, "managerDN");
    protected final Control managerPassword = control("configurations/managerPasswordSecret" /* >= 1.16 */, "managerPasswordSecret" /* >=1.9 */, "managerPassword");
    protected final Control userSearchBase = control("configurations/userSearchBase" /* >= 1.16 */, "userSearchBase");
    protected final Control userSearchFilter = control("configurations/userSearch" /* >= 1.16 */, "userSearch");
    protected final Control groupSearchBase = control("configurations/groupSearchBase" /* >= 1.16 */, "groupSearchBase");
    protected final Control groupSearchFilter = control("configurations/groupSearchFilter" /* >= 1.16 */, "groupSearchFilter");
    /**
     * only available prior ldap plugin version 1.10
     */
    protected final Control groupMembershipFilter  = control("groupMembershipFilter");
    protected final Control disableLdapEmailResolver = control("disableMailAddressResolver");
    protected final Control enableCache = control("cache");
    protected final Control addEnvVariableButton = control("configurations/repeatable-add" /* >= 1.16 */, "repeatable-add");
    /**
     * since version 1.8
     */
    private final Control displayNameAttributeName = control("configurations/displayNameAttributeName" /* >= 1.16 */, "displayNameAttributeName");
    /**
     * since version 1.8
     */
    private final Control mailAddressAttributeName = control("configurations/mailAddressAttributeName" /* >= 1.16 */, "mailAddressAttributeName");

    /**
     * For changes in GUI in version 1.16
     */
    private final boolean sinceVersion116 = isVersionEqualsOrGreater116();
    protected final Control advanced = control("advanced-button" /* >= 1.16 only */);

    public LdapSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
        this.context = context;
    }

    private boolean isVersionEqualsOrGreater116() {
        boolean isVersion116 = true;
        try {
            control("configurations/server").resolve();
        } catch (NoSuchElementException e) {
            isVersion116 = false;
        }

        return isVersion116;
    }

    private T useGroupMembershipStrategy(Class<T> type) {
        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override
            protected WebElement find(String caption) {
                return getElement(by.radioButton(caption));
            }
        });
        radio.click();

        try {
            // from radio label get input contained inside it
            String path = radio.findElement(By.tagName("input")).getAttribute("path");
            return newInstance(type, this.context, path);
        } catch (NoSuchElementException e) {
            return newInstance(type, this.context, radio.getAttribute("path"));
        }
    }

    /**
     * Fills the input fields for ldap access control.
     */
    public void configure(LdapDetails<T> ldapDetails) {
        ldapServer.set(ldapDetails.getHostWithPort());
        advancedServer.click();
        rootDn.set(ldapDetails.getRootDn());
        managerDn.set(ldapDetails.getManagerDn());
        managerPassword.set(ldapDetails.getManagerPassword());
        userSearchBase.set(ldapDetails.getUserSearchBase());
        userSearchFilter.set(ldapDetails.getUserSearchFilter());
        groupSearchBase.set(ldapDetails.getGroupSearchBase());
        groupSearchFilter.set(ldapDetails.getGroupSearchFilter());
        configureGroupMembership(ldapDetails);
        if (this.sinceVersion116) {
            advanced.click();
        }
        disableLdapEmailResolver.check(ldapDetails.isDisableLdapEmailResolver());
        if (ldapDetails.isEnableCache()) {
            enableCache.check(true);
            control("cache/size").select(String.valueOf(ldapDetails.getCacheSize()));
            control("cache/ttl").select(String.valueOf(ldapDetails.getCacheTTL()));
        }
        if (ldapDetails.getDisplayNameAttributeName() != null) {
            displayNameAttributeName.set(ldapDetails.getDisplayNameAttributeName());
        }
        if (ldapDetails.getMailAddressAttributeName() != null) {
            mailAddressAttributeName.set(ldapDetails.getMailAddressAttributeName());
        }
        if (ldapDetails.getEnvironmentVariables() != null && !ldapDetails.getEnvironmentVariables().isEmpty()) {
            int i = 0;
            String envVarSelector;
            for (LdapEnvironmentVariable envVariable : ldapDetails.getEnvironmentVariables()) {
                addEnvVariableButton.click();
                envVarSelector = i == 0 ? "" : "[" + i + "]";
                control("configurations/environmentProperties" + envVarSelector + "/name" /* >= 1.16 */, "/environmentProperties" + envVarSelector + "/name").set(envVariable.getName());
                control("configurations/environmentProperties" + envVarSelector + "/value" /* >= 1.16 */, "/environmentProperties" + envVarSelector + "/value").set(envVariable.getValue());
                i++;
            }
        }
    }

    /**
     * Subclasses can override this to handle group membership differently.
     */
    protected void configureGroupMembership(LdapDetails<T> ldapDetails) {
        if (ldapDetails.getGroupMembershipStrategy() != null) {
            T groupMembershipStrategy = useGroupMembershipStrategy(ldapDetails.getGroupMembershipStrategy());
            groupMembershipStrategy.configure(ldapDetails.getGroupMembershipStrategyParam());
        }
    }

}
