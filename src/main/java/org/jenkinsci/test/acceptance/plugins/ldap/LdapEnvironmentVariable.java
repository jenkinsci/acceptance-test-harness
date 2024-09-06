package org.jenkinsci.test.acceptance.plugins.ldap;

/**
 * Represents an environment variable for the LDAP plugin.
 */
public class LdapEnvironmentVariable {

    public static final String READ_TIMEOUT = "com.sun.jndi.ldap.read.timeout";
    public static final String CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

    private String name;
    private String value;

    public LdapEnvironmentVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
