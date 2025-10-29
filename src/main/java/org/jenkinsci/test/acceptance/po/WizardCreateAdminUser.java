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

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import com.google.inject.Inject;
import java.time.Duration;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for Wizard Create admin user
 *
 */
public class WizardCreateAdminUser extends PageObject {

    Control cUsername = control(by.name("username"));
    Control cPassword1 = control(by.name("password1"));
    Control cPassword2 = control(by.name("password2"));
    Control cFullName = control(by.name("fullname"));
    Control cEmail = control(by.name("email"));

    @Inject
    JenkinsController controller;

    public WizardCreateAdminUser(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url(""));
    }

    public WizardCreateAdminUser createAdminUser(String userName, String password, String fullName, String email) {
        driver.switchTo().defaultContent().switchTo().frame("setup-first-user");

        cUsername.set(userName);
        cPassword1.set(password);
        cPassword2.set(password);
        cFullName.set(fullName);

        if (cEmail.exists()) {
            cEmail.set(email);
        }

        driver.switchTo().defaultContent();
        control(by.css(".btn-primary.save-first-user")).clickAndWaitToBecomeStale(Duration.ofSeconds(time.seconds(5)));
        return this;
    }

    public void confirmURLSettings() {
        By confirmJenkinsUrl = by.css(".btn-primary.save-configure-instance");
        waitFor(driver)
                .withTimeout(Duration.ofSeconds(time.seconds(2)))
                .withMessage("Unable to locate the save button to configure jenkins url")
                .until(ExpectedConditions.presenceOfElementLocated(confirmJenkinsUrl));
        waitFor(driver)
                .withTimeout(Duration.ofSeconds(time.seconds(2)))
                .withMessage("Unable to click the save button to configure jenkins url")
                .until(ExpectedConditions.elementToBeClickable(confirmJenkinsUrl));
        Control control = control(confirmJenkinsUrl);
        try {
            control.clickAndWaitToBecomeStale(Duration.ofSeconds(time.seconds(30)));
        } catch (TimeoutException ex) {
            System.err.println("The button to accept the url settings in the setup wizard is not becoming stale");
        }
    }

    public void wizardShouldFinishSuccessfully() {
        By installDoneButtonSelector = by.css(".btn-primary.install-done");
        waitFor(installDoneButtonSelector);
        control(installDoneButtonSelector).click();
        waitFor(driver, hasContent("Welcome to Jenkins!"), 30);
    }
}
