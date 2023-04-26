package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.junit.Assert.assertTrue;

/**
 * Tests related to the remoting serialization filter.
 */
public class ClassFilterBlacklistTest extends AbstractJUnitTest {

    @Test
    @Issue("SECURITY-360")
    public void checkBlacklist() {
        assertTrue("com.sun.jndi.ldap.LDAPAttribute should be blacklisted", isBlacklisted("com.sun.jndi.ldap.LDAPAttribute"));
        assertTrue("java.security.* is not blacklisted", !isBlacklisted("java.security.Certificate"));
    }

    private boolean isBlacklisted(String name) {
        final String output = jenkins.runScript(String.format("hudson.remoting.ClassFilter.DEFAULT.check(\"%s\")", name));
        return !output.equals(name);
    }

}
