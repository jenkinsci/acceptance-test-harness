package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapEnvironmentVariable;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapGroupMembershipStrategy;
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

    protected Control ldapServer;
    protected Control advanced;
    protected Control advancedServer;
    protected Control rootDn;
    protected Control managerDn;
    protected Control managerPassword;
    protected Control userSearchBase;
    protected Control userSearchFilter;
    protected Control groupSearchBase;
    protected Control groupSearchFilter;
    /**
     * only available prior ldap plugin version 1.10
     */
    protected Control groupMembershipFilter;
    protected Control disableLdapEmailResolver;
    protected Control enableCache;
    protected Control addEnvVariableButton;
    /**
     * since version 1.8
     */
    private Control displayNameAttributeName;
    /**
     * since version 1.8
     */
    private Control mailAddressAttributeName;

    /**
     * For changes in GUI in version 1.16
     */
    private final boolean version116 = isVersionEqualsOrGreater116();

    public LdapSecurityRealm(GlobalSecurityConfig context, String path) {
        super(context, path);
        this.context = context;
        initControls();
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

    /*
     * Controls for configureSecurity page must be setted depending on the current version of ldap plugins due to changes in GUI
     */
    private void initControls() {
        if(this.version116) {
            this.ldapServer = control("configurations/server");
            this.advanced = control("advanced-button");
            this.advancedServer = control("configurations/advanced-button");
            this.rootDn = control("configurations/rootDN");
            this.managerDn = control("configurations/managerDN");
            this.managerPassword = control("configurations/managerPasswordSecret"/* >= 1.9*/, "configurations/managerPassword");
            this.userSearchBase = control("configurations/userSearchBase");
            this.userSearchFilter = control("configurations/userSearch");
            this.groupSearchBase = control("configurations/groupSearchBase");
            this.groupSearchFilter = control("configurations/groupSearchFilter");
            this.addEnvVariableButton = control("repeatable-add[1]");
            this.displayNameAttributeName = control("configurations/displayNameAttributeName");
            this.mailAddressAttributeName = control("configurations/mailAddressAttributeName");
        } else {
            this.ldapServer = control("server");
            this.advanced = control("advanced-button");
            this.advancedServer = null;
            this.rootDn = control("rootDN");
            this.managerDn = control("managerDN");
            this.managerPassword = control("managerPasswordSecret"/* >= 1.9*/, "configurations/managerPassword");
            this.userSearchBase = control("userSearchBase");
            this.userSearchFilter = control("userSearch");
            this.groupSearchBase = control("groupSearchBase");
            this.groupSearchFilter = control("groupSearchFilter");
            this.addEnvVariableButton = control("repeatable-add");
            this.displayNameAttributeName = control("displayNameAttributeName");
            this.mailAddressAttributeName = control("mailAddressAttributeName");
        }

        this.groupMembershipFilter = control("groupMembershipFilter");
        this.disableLdapEmailResolver = control("disableMailAddressResolver");
        this.enableCache = control("cache");
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
    public void configure(LdapDetails<T> ldapDetails) {
        ldapServer.set(ldapDetails.getHostWithPort());
        if(this.version116) {
            advancedServer.click();
        } else {
            advanced.click();
        }
        rootDn.set(ldapDetails.getRootDn());
        managerDn.set(ldapDetails.getManagerDn());
        managerPassword.set(ldapDetails.getManagerPassword());
        userSearchBase.set(ldapDetails.getUserSearchBase());
        userSearchFilter.set(ldapDetails.getUserSearchFilter());
        groupSearchBase.set(ldapDetails.getGroupSearchBase());
        groupSearchFilter.set(ldapDetails.getGroupSearchFilter());
        configureGroupMembership(ldapDetails);
        if(this.version116) {
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
                control("/environmentProperties" + envVarSelector + "/name").set(envVariable.getName());
                control("/environmentProperties" + envVarSelector + "/value").set(envVariable.getValue());
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
