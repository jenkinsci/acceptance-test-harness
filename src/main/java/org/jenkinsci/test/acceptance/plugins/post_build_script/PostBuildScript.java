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
package org.jenkinsci.test.acceptance.plugins.post_build_script;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.po.Step;
import org.openqa.selenium.WebElement;

@Describable({"Execute a set of scripts", "[PostBuildScript] - Execute a set of scripts"})
public class PostBuildScript extends PostBuildStep {
    private final Control whenSucceeded = control("scriptOnlyIfSuccess");
    private final Control whenFailed = control("scriptOnlyIfFailure");

    public PostBuildScript(Job parent, String path) {
        super(parent, path);
    }

    public <T extends Step> T addStep(Class<T> type) {

        final WebElement dropDown = control("hetero-list-add[buildStep]").resolve();
        findCaption(type, new Resolver() {
            @Override protected void resolve(String caption) {
                selectDropdownMenu(caption, dropDown);
            }
        });

        String path = last(by.xpath("//div[@name='buildStep']")).getAttribute("path");

        return newInstance(type, parent, path);
    }

    public void runWhenFailed() {
        whenSucceeded.uncheck();
        whenFailed.check();
    }

    public void runWhenSucceeded() {
        whenSucceeded.check();
        whenFailed.uncheck();
    }
}
