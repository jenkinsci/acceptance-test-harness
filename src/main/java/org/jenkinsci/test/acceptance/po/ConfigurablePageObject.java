/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import com.google.inject.Injector;
import groovy.lang.Closure;
import java.net.URL;
import java.util.concurrent.Callable;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * {@link PageObject} that can be configured and saved.
 * <p>
 * There are 2 uses of this:
 * - {@link ContainerPageObject}, PO that has /configure page associated
 * - PO that is itself a config page, such as /configureSecurity
 *
 * @author ogondza.
 */
public abstract class ConfigurablePageObject extends PageObject {

    private static final By SAVE_BUTTON = By.xpath(
            "//div[contains(@class, 'bottom-sticker-inner')]//input[@type='submit'] | //div[contains(@class, 'bottom-sticker-inner')]//button[contains(@name, 'Submit')]");

    protected ConfigurablePageObject(PageObject context, URL url) {
        super(context, url);
    }

    public ConfigurablePageObject(Injector injector, URL url) {
        super(injector, url);
    }

    /**
     * Edits this configurable page object using the specified configuration lambda. Opens the configuration view,
     * runs the specified body and saves the changes.
     *
     * @param body the additional configuration options for this page object
     */
    public void configure(final Runnable body) {
        configure();
        body.run();
        save();
    }

    /**
     * Edits this configurable page object using the specified closure. Opens the configuration view,
     * runs the specified body and saves the changes.
     *
     * @param body the additional configuration options for this page object
     */
    public void configure(final Closure body) {
        configure();
        body.call(this);
        save();
    }

    /**
     * Edits this configurable page object using the specified callable. Opens the configuration view,
     * runs the specified body and saves the changes.
     *
     * @param body the additional configuration options for this page object
     * @return return value of the body
     */
    public <T> T configure(final Callable<T> body) {
        try {
            configure();
            T v = body.call();
            save();
            return v;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Open configuration page if not yet opened.
     *
     * @see #getConfigUrl()
     */
    public void configure() {
        // Automatic disabling of sticky elements doesn't seem to occur after a redirect,
        // so force it after the configuration page has loaded
        new Scroller(driver).disableStickyElements();

        if (!driver.getCurrentUrl().equals(getConfigUrl().toExternalForm())) {
            visit(getConfigUrl());
        }
        waitFor(By.xpath("//form[contains(@name, '" + getFormName() + "')]"), 10);
        waitFor(SAVE_BUTTON, 5);
    }

    public String getFormName() {
        return "config";
    }

    /**
     * Makes sure that the browser is currently opening the configuration page.
     */
    public void ensureConfigPage() {
        assertThat(
                "config page is open", driver.getCurrentUrl(), is(getConfigUrl().toExternalForm()));
    }

    public abstract URL getConfigUrl();

    public void save() {
        WebElement e = find(SAVE_BUTTON);
        e.click();
        waitFor(e).until(CapybaraPortingLayerImpl::isStale);
    }

    public void apply() {
        clickButton("Apply");
        waitFor(driver, hasContent("Saved"), 30);
    }
}
