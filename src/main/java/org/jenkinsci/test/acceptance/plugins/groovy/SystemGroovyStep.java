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
package org.jenkinsci.test.acceptance.plugins.groovy;

import org.jenkinsci.test.acceptance.po.*;

@Describable("Execute system Groovy script")
public class SystemGroovyStep extends AbstractStep implements BuildStep {

    public SystemGroovyStep(Job parent, String path) {
        super(parent, path);
    }

    public SystemGroovyStep script(String script) {
        control("scriptSource[0]").check();
        new CodeMirror(parent, getPath() + "/scriptSource[0]/command").set(script);
        control("scriptSource[0]/validate-button").click();
        return this;
    }

    public SystemGroovyStep file(String path) {
        control("scriptSource[1]").check();
        control("scriptSource[1]/scriptFile").set(path);
        return this;
    }
}
