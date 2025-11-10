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
import java.util.List;
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
        WebElement installSuggested = waitFor(suggestedButtonLocator, 30);
        installSuggested.click();
        waitFor(installSuggested).until(CapybaraPortingLayerImpl::isStale);
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
                        WebElement element = driver.switchTo()
                                .defaultContent()
                                .switchTo()
                                .frame("setup-first-user")
                                .findElement(by.name("username"));
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
        waitFor().withTimeout(Duration.ofSeconds(1)).until(() -> isSelected(pluginKey));
    }

    public void startInstall() {
        clickButton("Install");
    }

    /**
     * @deprecated use {@link #selectNone()} instead.
     */
    @Deprecated
    public void deselectAll() {
        selectNone();
    }

    public void selectAll() {
        clickLink("All");
        waitFor()
                .withTimeout(Duration.ofSeconds(1))
                .until(() -> !getSelected().isEmpty() && getUnselected().isEmpty());
    }

    public void selectNone() {
        clickLink("None");
        waitFor()
                .withTimeout(Duration.ofSeconds(1))
                .until(() -> getSelected().isEmpty() && !getUnselected().isEmpty());
    }

    public void selectSuggested() {
        clickLink("Suggested");
        waitFor()
                .withTimeout(Duration.ofSeconds(1))
                .until(() -> !getSelected().isEmpty() && !getUnselected().isEmpty());
    }

    private boolean isSelected(String pluginKey) {
        return driver.findElement(by.id("chk-" + pluginKey)).isSelected();
    }

    private List<WebElement> getSelected() {
        return driver.findElements(
                by.xpath(
                        "//div[contains(@class, 'plugins-for-category')]/div[contains(@class, 'plugin') and contains(@class, 'selected')]"));
    }

    private List<WebElement> getUnselected() {
        return driver.findElements(
                by.xpath(
                        "//div[contains(@class, 'plugins-for-category')]/div[contains(@class, 'plugin') and not(contains(@class, 'selected'))]"));
    }
}
