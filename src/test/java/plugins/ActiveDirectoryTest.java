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
import org.jenkinsci.test.acceptance.plugins.active_directory.ActiveDirectorySecurity;
import org.jenkinsci.test.acceptance.plugins.active_directory.ActiveDirectorySecurityRealm;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.ProjectBasedMatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Set these (data) at mvn-test command line to use this test:<br>
 * <br>
 * - adBindDN="CN=user,OU=CA,OU=User,OU=P001,OU=ID,OU=Data,DC=company,DC=com" (optional; none => user@domain)<br>
 * - adController=host.domain.company.com:3269 (optional)<br>
 * - adDomain=company.com<br>
 * - adPassword=pwd<br>
 * - adSite=Site (optional)<br>
 * - adUser=user<br>
 * <br>
 * Consider setting -Dhudson.plugins.active_directory.ActiveDirectorySecurityRealm.forceLdaps=true<br>
 * <br>
 * You might need to disable security if previously enabled by a failed test run.<br>
 * Not doing so will likely make the (this) test fail the next time it is executed.<br>
 * That is because of the harness trying to handle FEP plugin, prior to running tests.<br>
 * (Likely caused by harness facing the unexpectedly enforced login page indeed.)
 *
 * @author Marco.Miller@ericsson.com
 */
@WithPlugins("active-directory")
public class ActiveDirectoryTest extends AbstractJUnitTest {
    private ActiveDirectorySecurity adSecurity;

    @Before
    public void setUp() {
        adSecurity = new ActiveDirectorySecurity(jenkins);
    }

    /**
     * Scenario: user can log-in to Jenkins as admin after AD security configured-<br>
     * Given a Jenkins instance that is of type=existing<br>
     *  And a pre-installed active-directory plugin version 1.34 (1.37 failed)<br>
     *  And an AD security configuration that is matrix-based (project)<br>
     *  And a user added to that matrix so she can Administer<br>
     * When I save such an AD security configuration<br>
     * Then that user can log-in to that Jenkins as admin.
     */
    @Test
    public void user_can_login_to_Jenkins_as_admin_after_AD_security_configured() {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();//open
        ActiveDirectorySecurityRealm realm = security.useRealm(ActiveDirectorySecurityRealm.class);
        realm.configure();
        ProjectBasedMatrixAuthorizationStrategy auth = security.useAuthorizationStrategy(ProjectBasedMatrixAuthorizationStrategy.class);
        MatrixRow userAuth = auth.addUser(ActiveDirectoryEnv.get().getUser());
        userAuth.admin();
        security.save();
        doLoginDespiteNoPathsThenWaitForLdap();
        security.configure();
        WebElement domain = getElement(by.name("_.domain"));
        assertNotNull(domain);
        assertEquals(ActiveDirectoryEnv.get().getDomain(),domain.getAttribute("value"));
    }

    /**
     * Scenario: TODO add-ing test(s) for user/group; work in progress
     */
    //@Test

    @After
    public void tearDown() {
        adSecurity.stopUsingSecurityAndSave();
    }

    private void doLoginDespiteNoPathsThenWaitForLdap() {
        jenkins.login();//TODO .doLogin() fails w/ my 1.554.1 => this method
        driver.findElement(by.name("j_username")).sendKeys(ActiveDirectoryEnv.get().getUser());
        driver.findElement(by.name("j_password")).sendKeys(ActiveDirectoryEnv.get().getPassword());
        clickButton("log in");
    }
}
