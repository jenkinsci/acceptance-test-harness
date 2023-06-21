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
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebElement;

/**
 * Page object for Wizard Customize Jenkins Page.
 *
 */
public class WizardCustomizeJenkins extends PageObject {

    public WizardCustomizeJenkins(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url(""));
    }

    public WizardCustomizeJenkins doInstallSuggested() {
        By suggestedButtonLocator = by.partialLinkText("Install suggested plugins");
        waitFor(suggestedButtonLocator, 30);
        Control installSuggested = control(suggestedButtonLocator);
        installSuggested.click();
        return this;
    }

    public WizardCustomizeJenkins doSelectPluginsToInstall() {
        By selectPluginsLocator = by.partialLinkText("Select plugins to install");
        waitFor(selectPluginsLocator, 30);
        Control selectPlugins = control(selectPluginsLocator);
        selectPlugins.click();
        waitFor(hasContent("Plugin Manager"));
        return this;
    }

    public void shouldFinishInstallSuccessfully() {
        waitFor()
                .withTimeout(Duration.ofSeconds(400))
                .ignoring(NoSuchElementException.class, NoSuchFrameException.class)
                .until(() -> {
                    try {
                        WebElement element = driver.switchTo().defaultContent().switchTo().frame("setup-first-user").findElement(by.name("username"));
                        return element != null;
                    } finally {
                        driver.switchTo().defaultContent();
                        assertThat(driver, not(hasContent("Installation Failures")));
                    }
                });
    }

    public void searchPlugin(String searchSring) {
        control(by.name("searchbox")).set(searchSring);
        elasticSleep(200);
    }

    public void selectPlugin(String pluginKey) {
        control(by.name(pluginKey)).click();
        elasticSleep(200);
    }

    public void startInstall() {
        clickButton("Install");
    }

    public void deselectAll() {
        clickLink("None");
        elasticSleep(200);
    }
}
