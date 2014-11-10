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
package plugins.no_docker;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.utils.pluginTests.SecurityDisabler;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapEnv;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.LdapSecurityRealm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasLoggedInUser;
import static org.jenkinsci.test.acceptance.utils.pluginTests.SecurityConfigUtils.*;

/**
 * Set these (data) at mvn-test command line to use this test:<br>
 * <br>
 * -ldapUser (mandatory)<br>
 * -ldapPassword (mandatory)<br>
 * -ldapGroup (mandatory)<br>
 * -ldapHost (mandatory)<br>
 * -ldapPort (mandatory)<br>
 * -ldapManagerDn (mandatory)<br>
 * -ldapManagerPassword (mandatory)<br>
 * -ldapRootDn (mandatory)<br>
 * -ldapUserSearchBase<br>
 * -ldapUserSearchFilter<br>
 * -ldapGroupSearchBase<br>
 * -ldapGroupSearchFilter<br>
 * -ldapGroupMembershipStrategy (ParseUserAttribute or SearchForGroups, case insensitive)<br>
 * -ldapGroupMembershipStrategyParam<br>
 * -ldapGroupMembershipFilter<br>
 * -ldapDisplayNameAttributeName<br>
 * -ldapMailAddressAttributeName<br>
 * -ldapDisableLdapEmailResolver (true or false, default false)<br>
 * -ldapEnableCache (true or false, default false)<br>
 * -ldapCacheSize (default 20) <br>
 * -ldapCacheTTL (default 300)<br>
 * <br>
 * Consider setting -Dhudson.plugins.active_directory.ActiveDirectorySecurityRealm.forceLdaps=true<br>
 * <br>
 * Test(s) disables security once over.
 *
 * @author Bowen.Cheng@ericsson.com
 */
@WithPlugins("ldap@1.10")
public class LdapPluginNoDockerTest extends AbstractJUnitTest {

    private SecurityDisabler securityDisabler;
    private LdapEnv ldapEnv = LdapEnv.getLdapEnv();
    private LdapDetails ldapDetails = LdapEnv.getLdapDetails();

    @Before
    public void setUp() {
        securityDisabler = new SecurityDisabler(jenkins);
    }

    /**
     * Scenario1: user can log-in to Jenkins as admin after LDAP security configured-<br>
     * Given a Jenkins instance<br>
     *  And an LDAP plugin version 1.10 (or greater)<br>
     *  And an LDAP security configuration that is matrix-based (project)<br>
     *  And a user added to that matrix so she can Administer<br>
     * When I save such an LDAP security configuration<br>
     * Then that user can log-in to that Jenkins as admin.
     */
    @Test
    public void user_can_login_to_Jenkins_as_admin_after_LDAP_security_configured() {
        userCanLoginToJenkinsAsAdmin(LdapEnv.getLdapEnv().getUser());
    }

    /**
     * Scenario2: user can log-in to Jenkins as admin group member after LDAP security configured-<br>
     * Given a Jenkins instance<br>
     *  And an LDAP plugin version 1.10 (or greater)<br>
     *  And an LDAP security configuration that is matrix-based (project)<br>
     *  And a group added to that matrix so its members can Administer<br>
     *  And a user being a member of that group<br>
     * When I save such an LDAP security configuration<br>
     * Then that user can log-in to that Jenkins as admin.
     */
    @Test
    public void user_can_login_to_Jenkins_as_admin_group_member_after_AD_security_configured() {
        userCanLoginToJenkinsAsAdmin(ldapEnv.getGroup());
    }

    /**
     * Scenario3: user wannabe cannot log-in to Jenkins after LDAP security configured-<br>
     * Given a Jenkins instance<br>
     *  And an LDAP plugin version 1.10 (or greater)<br>
     *  And an LDAP security configuration that is matrix-based (project)<br>
     *  And a wannabe added to that matrix thinking he can Administer<br>
     * When I save such an LDAP security configuration<br>
     * Then that user wannabe cannot log-in to that Jenkins at all.
     */
    @Test
    public void wannabe_cannot_login_to_Jenkins_after_LDAP_security_configured() {
        userCanLoginToJenkinsAsAdmin(ldapEnv.getUser());
        String userWannabe = ldapEnv.getUser()+"-wannabe";
        GlobalSecurityConfig security = saveSecurityConfig(userWannabe);
        jenkins.logout();
        jenkins.login().doLoginDespiteNoPaths(userWannabe, ldapEnv.getPassword());
        security.configure();
        jenkins.login().doLoginDespiteNoPaths(ldapEnv.getUser(), ldapEnv.getPassword());
    }

    @After
    public void tearDown() {
        securityDisabler.stopUsingSecurityAndSave();
    }

    private void userCanLoginToJenkinsAsAdmin(String userOrGroupToAddAsAdmin) {
        saveSecurityConfig(userOrGroupToAddAsAdmin);
        jenkins.login().doLoginDespiteNoPaths(ldapEnv.getUser(), ldapEnv.getPassword());
        assertThat(jenkins, hasLoggedInUser(ldapEnv.getUser()));
    }

    private GlobalSecurityConfig saveSecurityConfig(String userOrGroupToAddAsAdmin) {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        security = configSecurityRealm(security);
        security = authorizeUserAsAdmin(userOrGroupToAddAsAdmin, security);
        security.save();
        return security;
    }

    private GlobalSecurityConfig configSecurityRealm(GlobalSecurityConfig security) {
        LdapSecurityRealm realm = security.useRealm(LdapSecurityRealm.class);
        realm.configure(ldapDetails);
        return security;
    }
}
