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
package org.jenkinsci.test.acceptance.plugins.matrix_reloaded;

import static org.hamcrest.MatcherAssert.assertThat;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.po.Action;
import org.jenkinsci.test.acceptance.po.ActionPageObject;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.MatrixBuild;
import org.openqa.selenium.By;

@ActionPageObject("matrix-reloaded")
public class MatrixReloadedAction extends Action {

    public MatrixReloadedAction(MatrixBuild parent, String relative) {
        super(parent, relative);
    }

    public MatrixReloadedAction shouldHaveCombination(String combination) {
        open();
        assertThat(driver, Matchers.hasElement(checkbox(combination)));
        return this;
    }

    private By checkbox(String combination) {
        return by.path("/MRP::" + combination);
    }

    public void rebuild(String... combinations) {
        open();
        for (String c : combinations) {
            control(checkbox(c)).check();
        }
        clickButton("Rebuild Matrix");
    }

    @Override
    public boolean isApplicable(ContainerPageObject po) {
        return po instanceof MatrixBuild;
    }
}
