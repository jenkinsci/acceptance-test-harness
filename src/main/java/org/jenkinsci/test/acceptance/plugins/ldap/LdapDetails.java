package org.jenkinsci.test.acceptance.plugins.ldap;

/**
 * POJO for LDAP server connection, credential and configuration details.
 *
 * @author Michael Prankl
 */
public class LdapDetails {

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

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getHostWithPort() {
        return hostWithPort;
    }

    public void setHostWithPort(final String hostWithPort) {
        this.hostWithPort = hostWithPort;
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

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(final String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(final String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(final String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupMembershipFilter() {
        return groupMembershipFilter;
    }

    public void setGroupMembershipFilter(final String groupMembershipFilter) { this.groupMembershipFilter = groupMembershipFilter; }

    public boolean isDisableLdapEmailResolver() {
        return disableLdapEmailResolver;
    }

    public void setDisableLdapEmailResolver(final boolean disableLdapEmailResolver) { this.disableLdapEmailResolver = disableLdapEmailResolver; }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(final boolean enableCache) {
        this.enableCache = enableCache;
    }

    public int getCacheSize() { return cacheSize; }

    public void setCacheSize(int cacheSize) { this.cacheSize = cacheSize; }

    public int getCacheTTL() { return cacheTTL; }

    public void setCacheTTL(int cacheTTL) { this.cacheTTL = cacheTTL; }
}
