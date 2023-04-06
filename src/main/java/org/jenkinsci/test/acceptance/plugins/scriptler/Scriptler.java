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
package org.jenkinsci.test.acceptance.plugins.scriptler;

import java.util.Map;

import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.po.Action;
import org.jenkinsci.test.acceptance.po.ActionPageObject;
import org.jenkinsci.test.acceptance.po.CodeMirror;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;

@ActionPageObject("scriptler")
public class Scriptler extends Action {

    public Scriptler(Jenkins parent, String relative) {
        super(parent, relative);
    }

    public Script upload(Resource script) {
        visit(url("scriptSettings"));
        new Control(injector, by.name("file")).upload(script);
        clickButton("Upload");

        return new Script(this, script.getName());
    }

    public Script create(String name, String text, Map<String, String> params) {
        visit(url("scriptSettings"));
        name += ".groovy";
        control("/id").set(name);
        control("/name").set(name);
        new CodeMirror(this, "/script").set(text);
        clickButton("Submit");

        Script script = new Script(this, name);

        if (params != null && !params.isEmpty()) {
            script.configureParams(params);
        }

        return script;
    }

    @Override
    public boolean isApplicable(ContainerPageObject po) {
        return po instanceof Jenkins;
    }
}
