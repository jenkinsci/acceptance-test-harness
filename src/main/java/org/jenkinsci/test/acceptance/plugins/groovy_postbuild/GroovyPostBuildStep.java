/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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
package org.jenkinsci.test.acceptance.plugins.groovy_postbuild;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Groovy post build step.
 */
@Describable("Groovy Postbuild")
public final class GroovyPostBuildStep extends AbstractStep implements PostBuildStep {
    /** Action regarding build status if script fails. */
    public static final String DO_NOTHING = "0";
    /** Action regarding build status if script fails. */
    public static final String UNSTABLE = "1";
    /** Action regarding build status if script fails. */
    public static final String FAILED = "2";

    private final Control script = control("script/script");
    private final Control sandbox = control("script/sandbox");
    private final Control behavior = control("behavior");

    public GroovyPostBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public GroovyPostBuildStep setScript(String script) {
        this.script.set(script);
        return this;
    }

    public GroovyPostBuildStep setSandbox(boolean useSandbox) {
        sandbox.check(useSandbox);
        return this;
    }

    public GroovyPostBuildStep setBehavior(String behavior) {
        this.behavior.select(behavior);
        return this;
    }
}
