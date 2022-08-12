/*
 * The MIT License
 *
 * Copyright 2014 Sony Mobile Communications Inc.
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
package org.jenkinsci.test.acceptance.plugins.copyartifact;


import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Convenience methods for testing the Copy Artifacts plugin.
 * @author <a href="tomas.westling@sonymobile.com">Tomas Westling</a>
 */
@Describable("Copy artifacts from another project")
public class CopyArtifactBuildStep extends AbstractStep implements BuildStep {

    /**
     * Standard constructor.
     * @param parent the parent Job page.
     * @param path the path that points to this page area.
     */
    public CopyArtifactBuildStep(Job parent, String path) {
        super(parent, path);
    }

    /**
     * Fill in the projectName text box.
     * @param value a string for the text box.
     * @return this.
     */
    public CopyArtifactBuildStep projectName(String value) {
        control("projectName").set(value);
        return this;
    }

    /**
     * Fill in the includes text box.
     * @param value a string for the text box.
     * @return this.
     */
    public CopyArtifactBuildStep includes(String value) {
        control("filter").set(value);
        return this;
    }

    /**
     * Fill in the excludes text box.
     * @param value a string for the text box.
     * @return this.
     */
    public CopyArtifactBuildStep excludes(String value) {
        control("excludes").set(value);
        return this;
    }

    /**
     * Fill in the target text box.
     * @param value a string for the text box.
     * @return this.
     */
    public CopyArtifactBuildStep targetDir(String value) {
        control("target").set(value);
        return this;
    }

    /**
     * Fill in the parameters text box.
     * @param value a string for the text box.
     * @return this.
     */
    public CopyArtifactBuildStep parameterFilters(String value) {
        control("parameters").set(value);
        return this;
    }

    /**
     * Check or uncheck the flatten directories button.
     * @param flatten check or uncheck.
     * @return this.
     */
    public CopyArtifactBuildStep flatten(boolean flatten) {
        control("flatten").check(flatten);
        return this;
    }
}
