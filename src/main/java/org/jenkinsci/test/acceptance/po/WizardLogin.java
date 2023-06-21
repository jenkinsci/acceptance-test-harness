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

package org.jenkinsci.test.acceptance.po;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.junit.Assume;

/**
 * Page object for Wizard Login page.
 *
 */
public class WizardLogin extends PageObject {

    private Control cPassword = control("/j_password");
    private Control cLogin = control("/Continue");

    public WizardLogin(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url(""));
    }

    /**
     * Login assuming "path" form element availability.<br>
     * Paths are usually available when using the default Jenkins controller.<br>
     * (Available thanks to pre-installed form-element-path plugin.)
     */
    public WizardLogin doLogin(String password) {
        cPassword.set(password);
        cLogin.click();
        return this;
    }

    /**
     * Login assuming "path" form element unavailability.<br>
     * Paths are usually unavailable when using the "existing" Jenkins controller.<br>
     * (Unavailable despite pre-installed form-element-path plugin.)
     */
    public WizardLogin doLoginDespiteNoPaths(String password) {
        driver.findElement(by.name("j_password")).sendKeys(password);
        clickButton("Continue");
        return this;
    }

    public WizardLogin doSuccessfulLogin(String password) {
        this.doLoginDespiteNoPaths(password);
        assertThat(driver, not(hasContent("The password entered is incorrect")));
        return this;
    }

    /**
     * Gets the generated password from the file on disk
     * 
     * @return the read password
     * @throws IOException if there is an IO error 
     */
    public String getPassword(JenkinsController controller) throws IOException {
        Assume.assumeThat("Testing the setup wizard is only supported if a LocalController is in use. Test will be skipped.", controller, instanceOf(LocalController.class));
        File passwordFile = new File(((LocalController) controller).getJenkinsHome(), "secrets/initialAdminPassword");
        return FileUtils.readFileToString(passwordFile, StandardCharsets.UTF_8).trim();
    }
}
