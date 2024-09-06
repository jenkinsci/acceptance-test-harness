/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees
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
package org.jenkinsci.test.acceptance.plugins.mstestrunner;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

@Describable("Run unit tests with MSTest")
public class MSTestRunnerBuildStep extends AbstractStep implements BuildStep {

    public final Control testFilesControl = control("testFiles");
    public final Control categoriesControl = control("categories");
    public final Control resultFileControl = control("resultFile");
    public final Control cmdLineArgsControl = control("cmdLineArgs");
    public final Control continueOnFailControl = control("continueOnFail");

    public MSTestRunnerBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public void configure(
            String testFiles, String resultFile, String categories, String cmdLineArgs, boolean ignoreFailingTests) {
        this.resultFileControl.set(resultFile);
        this.testFilesControl.set(testFiles);
        this.continueOnFailControl.check(ignoreFailingTests);
        if (StringUtils.isNotBlank(categories)) {
            this.categoriesControl.set(categories);
        }
        if (StringUtils.isNotBlank(cmdLineArgs)) {
            this.cmdLineArgsControl.set(cmdLineArgs);
        }
    }
}
