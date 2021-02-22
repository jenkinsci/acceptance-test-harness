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
import org.jenkinsci.test.acceptance.utils.pluginTests.SecurityDisabler;
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
    // This should ideally use @TestActivation but it requires nonstandard properties so failing assumption in instance initialization instead.
    private final ActiveDirectoryEnv ENV = ActiveDirectoryEnv.get();

    private SecurityDisabler securityDisabler;

    @Before
    public void setUp() {
        securityDisabler = new SecurityDisabler(jenkins);
    }

    @Test
    public void user_can_login_to_Jenkins_as_admin_after_AD_security_configured() {
        userCanLoginToJenkinsAsAdmin(ENV.getUser());
    }

    @Test
    public void user_can_login_to_Jenkins_as_admin_group_member_after_AD_security_configured() {
        userCanLoginToJenkinsAsAdmin(ENV.getGroup());
    }

    @Test
    public void wannabe_cannot_login_to_Jenkins_after_AD_security_configured() {
        userCanLoginToJenkinsAsAdmin(ENV.getUser());
        String userWannabe = ENV.getUser() + "-wannabe";
        GlobalSecurityConfig security = saveSecurityConfig(userWannabe);
        jenkins.logout();
        jenkins.login().doLogin(userWannabe,
                ENV.getPassword());
        security.configure();
        assertThat(getElement(by.name("_.domain")), is(nullValue()));
        jenkins.login().doLogin(ENV.getUser(),
                ENV.getPassword());
    }

    @After
    public void tearDown() {
        securityDisabler.stopUsingSecurityAndSave();
    }

    private void userCanLoginToJenkinsAsAdmin(String userOrGroupToAddAsAdmin) {
        GlobalSecurityConfig security = saveSecurityConfig(userOrGroupToAddAsAdmin);
        jenkins.login().doLogin(ENV.getUser(),
                ENV.getPassword());
        security.configure();
        WebElement domain = getElement(by.name("_.domain"));
        assertThat(domain, is(notNullValue()));
        assertThat(domain.getAttribute("value"), is(equalTo(ENV.getDomain())));
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
