package org.jenkinsci.test.acceptance.plugins.ldap;

import java.util.LinkedList;
import java.util.List;

/**
 * POJO for LDAP server connection, credential and configuration details.
 *
 * @author Michael Prankl
 */
public class LdapDetails<T extends LdapGroupMembershipStrategy> {
    private String host;
    private int port;
    private String hostWithPort;
    private String managerDn;
    private String managerPassword;
    private String rootDn;
    private String userSearchBase;
    private String userSearchFilter;
    private String groupSearchBase;
    private String groupSearchFilter;
    private String groupMembershipFilter;
    private boolean disableLdapEmailResolver = false;
    private boolean enableCache = false;
    private int cacheSize = 20;
    private int cacheTTL = 300;
    private String mailAddressAttributeName;
    private String displayNameAttributeName;
    private Class<T> groupMembershipStrategy;
    private String groupMembershipStrategyParam;
    private List<LdapEnvironmentVariable> environmentVariables;

    public LdapDetails(String host, int port, String managerDn, String managerPassword, String rootDn) {
        this.setHost(host);
        this.setPort(port);
        this.setManagerDn(managerDn);
        this.setManagerPassword(managerPassword);
        this.setRootDn(rootDn);
        this.setHostWithPort(host + ":" + port);
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public LdapDetails host(final String host) {
        setHost(host);
        return this;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public LdapDetails port(final int port) {
        setPort(port);
        return this;
    }

    public String getHostWithPort() {
        return hostWithPort;
    }

    public void setHostWithPort(final String hostWithPort) {
        this.hostWithPort = hostWithPort;
    }

    public LdapDetails hostWithPort(final String hostWithPort) {
        setHostWithPort(hostWithPort);
        return this;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public void setManagerDn(final String managerDn) {
        this.managerDn = managerDn;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(final String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public String getRootDn() {
        return rootDn;
    }

    public void setRootDn(final String rootDn) {
        this.rootDn = rootDn;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(final String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public LdapDetails userSearchBase(final String userSearchBase) {
        setUserSearchBase(userSearchBase);
        return this;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(final String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public LdapDetails userSearchFilter(final String userSearchFilter) {
        setUserSearchFilter(userSearchFilter);
        return this;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(final String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public LdapDetails groupSearchBase(final String groupSearchBase) {
        setGroupSearchBase(groupSearchBase);
        return this;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(final String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public LdapDetails groupSearchFilter(final String groupSearchFilter) {
        setGroupSearchFilter(groupSearchFilter);
        return this;
    }

    public String getGroupMembershipFilter() {
        return groupMembershipFilter;
    }

    public void setGroupMembershipFilter(final String groupMembershipFilter) {
        this.groupMembershipFilter = groupMembershipFilter;
    }

    public LdapDetails groupMembershipFilter(final String groupMembershipFilter) {
        setGroupMembershipFilter(groupMembershipFilter);
        return this;
    }

    public boolean isDisableLdapEmailResolver() {
        return disableLdapEmailResolver;
    }

    public void setDisableLdapEmailResolver(final boolean disableLdapEmailResolver) {
        this.disableLdapEmailResolver = disableLdapEmailResolver;
    }

    public LdapDetails disableLdapEmailResolver(final boolean disableLdapEmailResolver) {
        setDisableLdapEmailResolver(disableLdapEmailResolver);
        return this;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(final boolean enableCache) {
        this.enableCache = enableCache;
    }

    public LdapDetails enableCache(final boolean enableCache) {
        setEnableCache(enableCache);
        return this;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public LdapDetails cacheSize(int cacheSize) {
        setCacheSize(cacheSize);
        return this;
    }

    public int getCacheTTL() {
        return cacheTTL;
    }

    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }

    public LdapDetails cacheTTL(int cacheTTL) {
        setCacheTTL(cacheTTL);
        return this;
    }

    public String getMailAddressAttributeName() {
        return mailAddressAttributeName;
    }

    public void setMailAddressAttributeName(String mailAddressAttributeName) {
        this.mailAddressAttributeName = mailAddressAttributeName;
    }

    public LdapDetails mailAdressAttributeName(String mailAddressAttributeName) {
        setMailAddressAttributeName(mailAddressAttributeName);
        return this;
    }

    public String getDisplayNameAttributeName() {
        return displayNameAttributeName;
    }

    public void setDisplayNameAttributeName(String displayNameAttributeName) {
        this.displayNameAttributeName = displayNameAttributeName;
    }

    public LdapDetails displayNameAttributeName(String displayNameAttributeName) {
        setDisplayNameAttributeName(displayNameAttributeName);
        return this;
    }

    public String getGroupMembershipStrategyParam() {
        return groupMembershipStrategyParam;
    }

    public void setGroupMembershipStrategyParam(String groupMembershipStrategyParam) {
        this.groupMembershipStrategyParam = groupMembershipStrategyParam;
    }

    public LdapDetails groupMembershipStrategyParam(String groupMembershipStrategyParam) {
        setGroupMembershipStrategyParam(groupMembershipStrategyParam);
        return this;
    }

    public Class<T> getGroupMembershipStrategy() {
        return groupMembershipStrategy;
    }

    public void setGroupMembershipStrategy(Class<T> groupMembershipStrategy) {
        this.groupMembershipStrategy = groupMembershipStrategy;
    }

    public LdapDetails groupMembershipStrategy(Class<T> groupMembershipStrategy) {
        setGroupMembershipStrategy(groupMembershipStrategy);
        return this;
    }

    public List<LdapEnvironmentVariable> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(List<LdapEnvironmentVariable> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public void addEnvironmentVariable(LdapEnvironmentVariable environmentVariable) {
        if (this.environmentVariables == null) {
            this.environmentVariables = new LinkedList<>();
        }
        this.environmentVariables.add(environmentVariable);
    }
}
