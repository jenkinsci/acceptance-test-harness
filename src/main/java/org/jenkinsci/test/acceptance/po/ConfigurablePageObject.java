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

import com.google.common.base.Predicate;
import com.google.inject.Injector;
import groovy.lang.Closure;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * {@link PageObject} that can be configured and saved.
 *
 * There are 2 uses of this:
 *   - {@link ContainerPageObject}, PO that has /configure page associated
 *   - PO that is itself a config page, such as /configureSecurity
 *
 * @author ogondza.
 */
public abstract class ConfigurablePageObject extends PageObject {
    protected ConfigurablePageObject(PageObject context, URL url) {
        super(context, url);
    }

    public ConfigurablePageObject(Injector injector, URL url) {
        super(injector, url);
    }

    public void configure(Closure body) {
        configure();
        body.call(this);
        save();
    }

    public <T> T configure(Callable<T> body) {
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
        if (driver.getCurrentUrl().equals(getConfigUrl().toExternalForm())) {
            return;
        }
        visit(getConfigUrl());
        elasticSleep(1000); // configure page requires some time to load
    }

    /**
     * Makes sure that the browser is currently opening the configuration page.
     */
    public void ensureConfigPage() {
        assertThat("config page is open", driver.getCurrentUrl(), is(getConfigUrl().toExternalForm()));
    }

    public abstract URL getConfigUrl();

    public void save() {
        clickButton("Save");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public void apply() {
        clickButton("Apply");
        waitFor(driver, hasContent("Saved"), 30);
    }
}
