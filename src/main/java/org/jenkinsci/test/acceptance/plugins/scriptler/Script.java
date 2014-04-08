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

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Node;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Can not be a PageObject due to plugin url scheme
public class Script extends CapybaraPortingLayer {

    private final Scriptler scriptler;
    private final String id;

    public Script(Scriptler scriptler, String id) {
        super(scriptler.injector);
        this.scriptler = scriptler;
        this.id = id;
    }

    public boolean exists() {
        scriptler.open();
        return getElement(by.xpath("//a[@href='runScript?id=%s']", id)) != null;
    }

    public ScriptResult run() {
        return runOn(null, null);
    }

    public ScriptResult runOn(Node node) {
        return runOn(null, node.getName());
    }

    public ScriptResult run(Map<String, String> params) {
        return runOn(params, null);
    }

    public ScriptResult runOnAllSlaves() {
        return runOn(null, "(all slaves)");
    }

    public ScriptResult runOnAllNodes() {
        return runOn(null, "(all)");
    }

    public ScriptResult runOn(Map<String, String> params, String slave) {
        visitAction("runScript");

        if (slave != null) {
            control("/node").select(slave);
        }

        if (params != null && !params.isEmpty()) {
            control("/defineParams").check();

            for (Map.Entry<String, String> pair: params.entrySet()) {
                control("/defineParams/parameters/name").set(pair.getKey());
                control("/defineParams/parameters/value").set(pair.getValue());
            }

            sleep(1000);
        }

        clickButton("Run");
        waitFor(by.xpath("//h2[text()='Result']"));

        By resultElement = by.xpath("//h2[text()='Result']/../pre");
        return new ScriptResult(find(resultElement).getText());
    }

    public void delete() {
        visitAction("removeScript");
    }

    private WebDriver visitAction(String action) {
        return visit(scriptler.url("%s?id=%s", action, id));
    }

    private Control control(String path) {
        return new Control(injector, by.path(path));
    }
}
