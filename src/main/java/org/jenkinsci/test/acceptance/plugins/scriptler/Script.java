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
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Node;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

// Can not be a PageObject due to plugin url scheme
public class Script extends CapybaraPortingLayerImpl {

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

    public ScriptResult runOnAllAgents() {
        return runOn(null, "(all slaves)");
    }

    public ScriptResult runOnAllNodes() {
        return runOn(null, "(all)");
    }

    public ScriptResult runOn(Map<String, String> params, String agent) {
        visitAction("runScript");

        if (agent != null) {
            try {
                control("/node").select(agent);
            }
            catch (NoSuchElementException ex) {
                elasticSleep(1000);
                visitAction("runScript");
                control("/node").select(agent);
            }
        }

        fillParams(params);

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

    public void configureParams(Map<String, String> params) {
        visitAction("editScript");
        setParams(params);
        clickButton("Submit");
    }

    private void fillParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return;
        }

        control("/defineParams").check();

        for (Map.Entry<String, String> pair : params.entrySet()) {

            WebElement elem = getElement(by.xpath("//input[@name='name' and @value='%s']", pair.getKey()));
            if (elem == null) {
                elem = getElement(by.xpath("//input[@name='name' and not(@value)]"));
                elem.sendKeys(pair.getKey());
            }

            String path = elem.getAttribute("path").replace("/name", "/value");
            control(path).set(pair.getValue());
        }
    }

    private void setParams(Map<String, String> params) {
        control("/defineParams").check();

        String prefix = "/defineParams/parameters/";
        for (Map.Entry<String, String> pair : params.entrySet()) {

            control(prefix + "name").set(pair.getKey());
            control(prefix + "value").set(pair.getValue());

            WebElement addButton = find(by.button("Add Parameter"));
            if (addButton != null) {
                addButton.click();

                elasticSleep(1000); // wait for new parameter to appear
                String path = find(by.button("Add Parameter")).getAttribute("path");
                prefix = path.substring(0, path.length() - 25);
            }
        }
    }
}
