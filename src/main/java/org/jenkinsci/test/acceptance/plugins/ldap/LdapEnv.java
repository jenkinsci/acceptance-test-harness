/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.plugins.ldap;

import static org.junit.Assume.assumeNotNull;

/**
 * Thread-unsafe singleton serving ldap-prefixed test env args.
 *
 * @author Bowen.Cheng@ericsson.com
 */
public class LdapEnv {

    private static final int DEFAULT_LDAP_PORT = 3268;
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String PARSE_USER_ATTRIBUTE_STRATEGY = "ParseUserAttribute";
    private static final String SEARCH_FOR_GROUPS_STRATEGY = "SearchForGroups";

    private static LdapDetails ldapDetails = null;
    private static LdapEnv ldapEnv = null;

    private String user;
    private String password;
    private String group;

    private LdapEnv() {
        user = System.getenv("ldapUser");
        password = System.getenv("ldapPassword");
        group = System.getenv("ldapGroup");
    }

    /**
     * @return Existing ldapEnv object or a newly constructed one with given environment variables
     */
    public static LdapEnv getLdapEnv() {
        if (ldapEnv == null) {
            ldapEnv = new LdapEnv();
        }
        return ldapEnv;
    }

    /**
     * @return Existing ldapDetails object or a newly constructed one with given environment variables
     */
    public static LdapDetails getLdapDetails() {
        if (ldapDetails == null) {
            ldapDetails = new LdapEnv().constructLdapDetails();
        }
        return ldapDetails;
    }

    /**
     * @return user name by "-ldapUser" attribute
     */
    public String getUser() {
        assumeNotNull(user);
        return user;
    }

    /**
     * @return password used for logging in by "-ldapPassword" attribute
     */
    public String getPassword() {
        assumeNotNull(password);
        return password;
    }

    /**
     * @return user group by "-ldapGroup" attribute
     */
    public String getGroup() {
        assumeNotNull(group);
        return group;
    }

    private LdapDetails constructLdapDetails() {
        String host = System.getenv("ldapHost");
        int port = parseInteger(System.getenv("ldapPort"), DEFAULT_LDAP_PORT);

        LdapDetails ldapDetails = new LdapDetails(
                host,
                port,
                System.getenv("ldapManagerDn"),
                System.getenv("ldapManagerPassword"),
                System.getenv("ldapRootDn"));
        ldapDetails.setHostWithPort(host + ":" + port);
        ldapDetails.setUserSearchBase(System.getenv("ldapUserSearchBase"));
        ldapDetails.setUserSearchFilter(System.getenv("ldapUserSearchFilter"));
        ldapDetails.setGroupSearchBase(System.getenv("ldapGroupSearchBase"));
        ldapDetails.setGroupSearchFilter(System.getenv("ldapGroupSearchFilter"));
        ldapDetails.setGroupMembershipStrategy(
                parseGroupMembershipStrategy(System.getenv("ldapGroupMembershipStrategy")));
        ldapDetails.setGroupMembershipStrategyParam(System.getenv("ldapGroupMembershipStrategyParam"));
        ldapDetails.setGroupMembershipFilter(System.getenv("ldapGroupMembershipFilter"));
        ldapDetails.setDisplayNameAttributeName(System.getenv("ldapDisplayNameAttributeName"));
        ldapDetails.setMailAddressAttributeName(System.getenv("ldapMailAddressAttributeName"));
        ldapDetails.setDisableLdapEmailResolver(parseBoolean(System.getenv("ldapDisableLdapEmailResolver"), false));
        ldapDetails.setEnableCache(parseBoolean(System.getenv("ldapEnableCache"), false));
        ldapDetails.setCacheSize(parseInteger(System.getenv("ldapCacheSize"), 20));
        ldapDetails.setCacheTTL(parseInteger(System.getenv("ldapCacheTTL"), 300));

        return ldapDetails;
    }

    private Class parseGroupMembershipStrategy(String strategy) {
        Class<? extends LdapGroupMembershipStrategy> strategyClass = null;

        if (isEmptyOrNullString(strategy)) {
            return strategyClass;
        }

        if (strategy.compareToIgnoreCase(PARSE_USER_ATTRIBUTE_STRATEGY) == 0) {
            strategyClass = ParseUserAttributeLdapGroupMembershipStrategy.class;
        } else if (strategy.compareToIgnoreCase(SEARCH_FOR_GROUPS_STRATEGY) == 0) {
            strategyClass = SearchForGroupsLdapGroupMembershipStrategy.class;
        }

        return strategyClass;
    }

    /**
     * Parse string representation of a boolean
     *
     * @param booleanString string representation of a boolean to be parsed
     * @param defaultValue the value to be returned if there is any parse exception
     * @return boolean value parsed
     */
    private boolean parseBoolean(String booleanString, boolean defaultValue) {
        boolean result = defaultValue;

        if (isEmptyOrNullString(booleanString)) {
            return result;
        }
        if (booleanString.compareToIgnoreCase(TRUE) == 0 || booleanString.compareToIgnoreCase(FALSE) == 0) {
            result = Boolean.parseBoolean(booleanString);
        }
        return result;
    }

    /**
     * Parse string representation of a integer
     *
     * @param intString string representation of a integer to be parsed
     * @param defaultValue  the value to be returned if there is any parse exception
     * @return integer value after parsed
     */
    private int parseInteger(String intString, int defaultValue) {
        int result;
        try {
            result = Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            result = defaultValue;
        }
        return result;
    }

    private boolean isEmptyOrNullString(String target) {
        return target == null || target.isEmpty();
    }
}
