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
package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.active_directory.ActiveDirectoryEnv;
import org.jenkinsci.test.acceptance.plugins.active_directory.ActiveDirectorySecurityRealm;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.ProjectBasedMatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Set these (data) at mvn-test command line to use this test:<br>
 * <br>
 * - adBindDN="CN=user,OU=CA,OU=User,OU=P001,OU=ID,OU=Data,DC=company,DC=com" (optional; none => user@domain)<br>
 * - adController=host.domain.company.com:3269 (optional)<br>
 * - adDomain=company.com<br>
 * - adGroup=groupToWhichUserBelongs (optional; none => skip group test)<br>
 * - adPassword=pwd<br>
 * - adSite=Site (optional)<br>
 * - adGroupLookupStrategy={ AUTO, RECURSIVE, CHAIN } (optional)<br>
 * - adUser=user<br>
 * <br>
 * Consider setting -Dhudson.plugins.active_directory.ActiveDirectorySecurityRealm.forceLdaps=true<br>
 * <br>
 * Test(s) disables security once over.<br>
 * You might need to disable security if previously enabled by a failed test run.<br>
 * Not doing so will likely make the (this) test fail the next time it is executed.<br>
 * That is because of the harness trying to handle FEP plugin, prior to running tests.<br>
 * (Likely caused by harness facing the unexpectedly enforced login page indeed.)
 *
 * @author Marco.Miller@ericsson.com
 */
@WithPlugins("active-directory@1.38")
public class ActiveDirectoryTest extends AbstractJUnitTest {
    private SecurityDisabler securityDisabler;

    @Before
    public void setUp() {
        securityDisabler = new SecurityDisabler(jenkins);
    }

    /**
     * Scenario1: user can log-in to Jenkins as admin after AD security configured-<br>
     * Given a Jenkins instance<br>
     * And an active-directory plugin version 1.38 (or greater)<br>
     * And an AD security configuration that is matrix-based (project)<br>
     * And a user added to that matrix so she can Administer<br>
     * When test button succeeded<br>
     * And I save such an AD security configuration<br>
     * Then that user can log-in to that Jenkins as admin.
     */
    @Test
    public void user_can_login_to_Jenkins_as_admin_after_AD_security_configured() {
        userCanLoginToJenkinsAsAdmin(ActiveDirectoryEnv.get().getUser());
    }

    /**
     * Scenario2: user can log-in to Jenkins as admin group member after AD security configured-<br>
     * Given a Jenkins instance<br>
     * And an active-directory plugin version 1.38 (or greater)<br>
     * And an AD security configuration that is matrix-based (project)<br>
     * And a group added to that matrix so its members can Administer<br>
     * And a user being a member of that group<br>
     * When test button succeeded<br>
     * And I save such an AD security configuration<br>
     * Then that user can log-in to that Jenkins as admin.
     */
    @Test
    public void user_can_login_to_Jenkins_as_admin_group_member_after_AD_security_configured() {
        userCanLoginToJenkinsAsAdmin(ActiveDirectoryEnv.get().getGroup());
    }

    /**
     * Scenario3: user wannabe cannot log-in to Jenkins after AD security configured-<br>
     * Given a Jenkins instance<br>
     * And an active-directory plugin version 1.38 (or greater)<br>
     * And an AD security configuration that is matrix-based (project)<br>
     * And a wannabe added to that matrix thinking he can Administer<br>
     * When test button succeeded<br>
     * And I save such an AD security configuration<br>
     * Then that user wannabe cannot log-in to that Jenkins at all.
     */
    @Test
    public void wannabe_cannot_login_to_Jenkins_after_AD_security_configured() {
        userCanLoginToJenkinsAsAdmin(ActiveDirectoryEnv.get().getUser());
        String userWannabe = ActiveDirectoryEnv.get().getUser() + "-wannabe";
        GlobalSecurityConfig security = saveSecurityConfig(userWannabe);
        jenkins.logout();
        jenkins.login().doLoginDespiteNoPaths(userWannabe,
                ActiveDirectoryEnv.get().getPassword());
        security.configure();
        assertThat(getElement(by.name("_.domain")), is(nullValue()));
        jenkins.login().doLoginDespiteNoPaths(ActiveDirectoryEnv.get().getUser(),
                ActiveDirectoryEnv.get().getPassword());
    }

    @After
    public void tearDown() {
        securityDisabler.stopUsingSecurityAndSave();
    }

    private void userCanLoginToJenkinsAsAdmin(String userOrGroupToAddAsAdmin) {
        GlobalSecurityConfig security = saveSecurityConfig(userOrGroupToAddAsAdmin);
        jenkins.login().doLoginDespiteNoPaths(ActiveDirectoryEnv.get().getUser(),
                ActiveDirectoryEnv.get().getPassword());
        security.configure();
        WebElement domain = getElement(by.name("_.domain"));
        assertThat(domain, is(notNullValue()));
        assertThat(domain.getAttribute("value"), is(equalTo(ActiveDirectoryEnv.get().getDomain())));
    }

    private GlobalSecurityConfig saveSecurityConfig(String userOrGroupToAddAsAdmin) {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        security = ProjectBasedMatrixAuthorizationStrategy.authorizeUserAsAdmin(userOrGroupToAddAsAdmin, security);
        security = configSecurityRealm(security);
        security.save();
        return security;
    }

    private GlobalSecurityConfig configSecurityRealm(GlobalSecurityConfig security) {
        ActiveDirectorySecurityRealm realm = security.useRealm(ActiveDirectorySecurityRealm.class);
        realm.configure();
        realm.validateConfig();
        return security;
    }
}
