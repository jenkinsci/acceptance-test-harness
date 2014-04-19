/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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

import org.openqa.selenium.WebDriver;

/**
 * Page object action.
 *
 * @author ogondza
 */
public abstract class Action extends PageObject {

    protected final ContainerPageObject parent;

    public Action(ContainerPageObject parent, String relative) {
        super(parent.injector, parent.url(relative + "/"));
        this.parent = parent;
    }

    @Override
    public WebDriver open() {
        WebDriver wd = super.open();

        if (!wd.getCurrentUrl().startsWith(url.toString())) {
            throw new AssertionError("Action " + url + " does not exist. Redirected to " + wd.getCurrentUrl());
        }

        return wd;
    }

    /**
     * @return true if and action can be attached to given page object.
     */
    public abstract boolean isApplicable(ContainerPageObject po);
}
