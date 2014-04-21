package org.jenkinsci.test.acceptance.plugins.ldap;

/**
 * Wrapper for LDAP server connection and credential details.
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
    private boolean enableLdapEmailResolver = true;
    private boolean enableCache = false;

    public LdapDetails(String host, int port, String managerDn, String managerPassword, String rootDn){
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

    public void setGroupMembershipFilter(final String groupMembershipFilter) {
        this.groupMembershipFilter = groupMembershipFilter;
    }

    public boolean isEnableLdapEmailResolver() {
        return enableLdapEmailResolver;
    }

    public void setEnableLdapEmailResolver(final boolean enableLdapEmailResolver) {
        this.enableLdapEmailResolver = enableLdapEmailResolver;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(final boolean enableCache) {
        this.enableCache = enableCache;
    }
}
