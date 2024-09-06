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

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.po.Step;
import org.openqa.selenium.By;

@Describable({
    "Execute scripts",
    "Execute Scripts",
    "Execute a set of scripts",
    "[PostBuildScript] - Execute a set of scripts"
})
public class PostBuildScript extends AbstractStep implements PostBuildStep {
    private final Control buildResult = control("buildSteps/results");

    public PostBuildScript(Job parent, String path) {
        super(parent, path);
    }

    public <T extends Step> T addStep(final Class<T> type) {
        String path = createPageArea(
                "buildSteps/buildSteps", () -> control("/buildSteps/hetero-list-add[postBuild.buildStep.buildSteps]")
                        .selectDropdownMenu(type));
        return newInstance(type, parent, path);
    }

    public void runWhenFailed() {
        if (buildResult
                .resolve()
                .findElement(By.xpath(".//option[@selected='true']"))
                .getAttribute("value")
                .equals("SUCCESS")) {
            // unselect default in multiple selection
            buildResult.select("SUCCESS");
        }
        buildResult.select("FAILURE");
    }

    public void runWhenSucceeded() {
        if (!buildResult
                .resolve()
                .findElement(By.xpath(".//option[@selected='true']"))
                .getAttribute("value")
                .equals("SUCCESS")) {
            buildResult.select("SUCCESS");
        }
    }
}
