/*
 * The MIT License
 *
 * Copyright 2017 CloudBees, Inc.
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
package core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.loggedInAs;

import java.io.IOException;

import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithInstallWizard;
import org.jenkinsci.test.acceptance.po.Login;
import org.jenkinsci.test.acceptance.po.WizardCreateAdminUser;
import org.jenkinsci.test.acceptance.po.WizardCustomizeJenkins;
import org.jenkinsci.test.acceptance.po.WizardLogin;
import org.junit.Test;

import com.google.inject.Inject;

@WithInstallWizard
public class InstallWizardTest extends AbstractJUnitTest {
    @Inject
    public JenkinsController controller;

    private static final String USERNAME = "adminuser";
    private static final String PASSWORD = "adminPassword";
    private static final String FULL_NAME = "admin full name";
    private static final String EMAIL = "admin@email.com";

    @Since("2.0")
    @Test
    public void wizardInstallSuggestedTest() throws IOException {

        jenkins.open();

        // Login step
        WizardLogin wizardLogin = new WizardLogin(jenkins);
        wizardLogin.doSuccessfulLogin(wizardLogin.getPassword(controller));

        // Customize Jenkins step
        WizardCustomizeJenkins wizardCustomize = new WizardCustomizeJenkins(jenkins);
        wizardCustomize.doInstallSuggested();

        wizardCustomize.shouldFinishInstallSuccessfully();

        // Create user test
        WizardCreateAdminUser createAdmin = new WizardCreateAdminUser(jenkins);

        createAdmin.createAdminUser(USERNAME, PASSWORD, FULL_NAME, EMAIL);
        createAdmin.shouldCreateTheUserSuccessfully();
        if(jenkins.getVersion().isNewerThan(new VersionNumber("2.118"))) {
            createAdmin.confirmURLSettings();
        }
        createAdmin.wizardShouldFinishSuccessfully();

        // Check that the new user is logged in
        Login login = new Login(jenkins);
        assertThat(login, loggedInAs("adminuser"));
    }

    @Since("2.0")
    @Test
    public void wizardInstallCustomPluginsTest() throws IOException {

        jenkins.open();

        // Login step
        WizardLogin wizardLogin = new WizardLogin(jenkins);
        wizardLogin.doSuccessfulLogin(wizardLogin.getPassword(controller));

        // Customize Jenkins step
        WizardCustomizeJenkins wizardCustomize = new WizardCustomizeJenkins(jenkins);
        wizardCustomize.doSelectPluginsToInstall();
        wizardCustomize.deselectAll();
        wizardCustomize.searchPlugin("MSBuild");
        wizardCustomize.selectPlugin("msbuild");
        wizardCustomize.startInstall();

        wizardCustomize.shouldFinishInstallSuccessfully();

        // Create user test
        WizardCreateAdminUser createAdmin = new WizardCreateAdminUser(jenkins);

        createAdmin.createAdminUser(USERNAME, PASSWORD, FULL_NAME, EMAIL);
        createAdmin.shouldCreateTheUserSuccessfully();
        if(jenkins.getVersion().isNewerThan(new VersionNumber("2.118"))) {
            createAdmin.confirmURLSettings();
        }
        createAdmin.wizardShouldFinishSuccessfully();

        // Check that the new user is logged in
        Login login = new Login(jenkins);
        assertThat(login, loggedInAs("adminuser"));
    }
}
